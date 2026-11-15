/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.javahelpers.codegen.processing;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.codegen.testing.ElementTestUtils.field;
import static org.javahelpers.codegen.testing.ElementTestUtils.method;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.javahelpers.codegen.testing.ProbeProcessor;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AnnotationProcessingContext} delegates against a real processing
 * environment.
 */
class AnnotationProcessingContextTest {

  private static final String DTO_SOURCE =
      """
      package test;
      /** Doc comment on the type. */
      public class Dto extends Base {
        @SuppressWarnings("unchecked") private String name;
        public String getName() { return name; }
      }
      class Base {
        public void inherited() {}
      }
      """;

  private static AnnotationProcessingContext context(
      javax.annotation.processing.ProcessingEnvironment env) {
    return new AnnotationProcessingContext(CapturingProcessingLogger.create().logger(), env);
  }

  @Test
  void delegates_elementAndTypeUtilities() {
    AtomicReference<List<Boolean>> results = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      AnnotationProcessingContext ctx = context(env);
                      TypeElement dto = ctx.getTypeElement("test.Dto");
                      TypeMirror nameType = field(dto, "name").asType();
                      TypeMirror dtoType = dto.asType();
                      results.set(
                          List.of(
                              dto != null,
                              ctx.getTypeElement(TypeName.of(String.class)) != null,
                              ctx.getPackageOf(dto).getQualifiedName().contentEquals("test"),
                              ctx.getPackageElement("test") != null,
                              ctx.getPackageName(dto).equals("test"),
                              ctx.getAllMembers(dto).stream()
                                  .anyMatch(e -> e.getSimpleName().contentEquals("inherited")),
                              ctx.getDocComment(dto) != null
                                  && ctx.getDocComment(dto).contains("Doc comment on the type."),
                              ((TypeElement) ctx.asElement(dtoType))
                                  .getQualifiedName()
                                  .equals(dto.getQualifiedName()),
                              ctx.isSameType(nameType, nameType),
                              !ctx.isSameType(nameType, dtoType),
                              ctx.erasure(nameType) != null,
                              ctx.isAssignable(ctx.erasure(dtoType), ctx.erasure(dtoType)),
                              !ctx.directSupertypes(dtoType).isEmpty()));
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    List<String> names =
        List.of(
            "typeElement",
            "typeElementByName",
            "packageOf",
            "packageElement",
            "packageName",
            "allMembers",
            "docComment",
            "asElement",
            "isSameType",
            "notSameType",
            "erasure",
            "isAssignable",
            "directSupertypes");
    for (int i = 0; i < names.size(); i++) {
      assertTrue(results.get().get(i), "check failed: " + names.get(i));
    }
  }

  @Test
  void generatedTypes_addFindAndClear() {
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      AnnotationProcessingContext ctx = context(env);
                      TypeElement dto = ctx.getTypeElement("test.Dto");
                      TypeName elementType = TypeName.of(String.class);
                      TypeName generated = TypeName.of(Integer.class);
                      assertTrue(ctx.resolveGeneratedType(dto).isEmpty());
                      ctx.addGeneratedType(elementType, generated);
                      assertTrue(ctx.resolveGeneratedType(dto).isEmpty()); // dto != elementType
                      assertTrue(ctx.getGeneratedTypes().findGenerated(elementType).isPresent());
                      ctx.getGeneratedTypes().clear();
                      assertTrue(ctx.getGeneratedTypes().findGenerated(elementType).isEmpty());
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
  }

  @Test
  void loggingDelegates_emitToLogger() {
    CapturingProcessingLogger capture = CapturingProcessingLogger.create();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      AnnotationProcessingContext ctx =
                          new AnnotationProcessingContext(capture.logger(), env);
                      ctx.info("hello %s", "info");
                      ctx.debug("hello %s", "debug");
                      ctx.warning("hello %s", "warning");
                      ctx.error("hello %s", "error");
                      ctx.startPerformancePhase();
                      ctx.endPerformancePhase("phase");
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    assertTrue(capture.messages().stream().anyMatch(m -> m.contains("hello info")));
    assertTrue(capture.messages().stream().anyMatch(m -> m.contains("hello warning")));
    assertTrue(capture.messages().stream().anyMatch(m -> m.contains("hello error")));
  }

  @Test
  void currentTask_nullByDefault() {
    AtomicReference<Object> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      AnnotationProcessingContext ctx = context(env);
                      result.set(ctx.getCurrentTask());
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    assertNull(result.get());
  }

  @Test
  void getElementValuesWithDefaults_includesDefaultsAndSetValues() {
    AtomicReference<Integer> size = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      AnnotationProcessingContext ctx = context(env);
                      javax.lang.model.element.VariableElement nameField =
                          field(typeElement(env, "test.Dto"), "name");
                      javax.lang.model.element.AnnotationMirror suppress =
                          nameField.getAnnotationMirrors().get(0);
                      size.set(ctx.getElementValuesWithDefaults(suppress).size());
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    // @SuppressWarnings declares a single 'value' member
    assertEquals(1, size.get());
  }

  @Test
  void isAccessibleFromGeneratedPackage_publicMembersAccessible() {
    AtomicReference<Boolean> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> {
                      AnnotationProcessingContext ctx = context(env);
                      TypeElement dto = typeElement(env, "test.Dto");
                      result.set(ctx.isAccessibleFromGeneratedPackage(method(dto, "getName")));
                    }))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    assertNotNull(result.get());
  }

  @Test
  void performanceTracker_defaultsToNoOp() {
    AtomicReference<Object> tracker = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, env) -> tracker.set(context(env).getPerformanceTracker())))
            .compile(CodegenTestUtils.forSource(DTO_SOURCE));
    assertThat(compilation).succeeded();
    assertNotNull(tracker.get());
    assertEquals("NoOpPerformanceTracker", tracker.get().getClass().getSimpleName());
  }
}
