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
import static org.javahelpers.codegen.processing.ProcessingTestUtils.context;
import static org.javahelpers.codegen.testing.ElementTestUtils.field;
import static org.javahelpers.codegen.testing.ElementTestUtils.method;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;
import org.javahelpers.codegen.testing.CodegenTestUtils;
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
        void inherited() {}
      }
      """;

  @Test
  void delegates_elementAndTypeUtilities() {
    AtomicReference<List<Boolean>> results = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.probe(
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
            },
            DTO_SOURCE);
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
        CodegenTestUtils.probe(
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
            },
            DTO_SOURCE);
    assertThat(compilation).succeeded();
  }

  @Test
  void loggingDelegates_emitToLogger() {
    CapturingProcessingLogger capture = CapturingProcessingLogger.create();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) -> {
              AnnotationProcessingContext ctx =
                  new AnnotationProcessingContext(capture.logger(), env);
              ctx.info("hello %s", "info");
              ctx.debug("hello %s", "debug");
              ctx.warning("hello %s", "warning");
              ctx.error("hello %s", "error");
              ctx.startPerformancePhase();
              ctx.endPerformancePhase("phase");
            },
            DTO_SOURCE);
    assertThat(compilation).succeeded();
    assertTrue(capture.messages().stream().anyMatch(m -> m.contains("hello info")));
    assertTrue(capture.messages().stream().anyMatch(m -> m.contains("hello warning")));
    assertTrue(capture.messages().stream().anyMatch(m -> m.contains("hello error")));
  }

  @Test
  void currentTask_nullByDefault() {
    AtomicReference<Object> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) -> {
              AnnotationProcessingContext ctx = context(env);
              result.set(ctx.getCurrentTask());
            },
            DTO_SOURCE);
    assertThat(compilation).succeeded();
    assertNull(result.get());
  }

  @Test
  void getElementValuesWithDefaults_includesDefaultsAndSetValues() {
    AtomicReference<Integer> elementsWithDefaults = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) -> {
              AnnotationProcessingContext ctx = context(env);
              VariableElement nameField = field(typeElement(env, "test.Dto"), "name");
              AnnotationMirror suppress = nameField.getAnnotationMirrors().get(0);
              elementsWithDefaults.set(ctx.getElementValuesWithDefaults(suppress).size());
            },
            DTO_SOURCE);
    assertThat(compilation).succeeded();
    // @SuppressWarnings declares a single 'value' member
    assertEquals(1, elementsWithDefaults.get());
  }

  @Test
  void isAccessibleFromGeneratedPackage_publicMembersAccessible() {
    AtomicReference<Boolean> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) -> {
              AnnotationProcessingContext ctx = context(env);
              TypeElement dto = typeElement(env, "test.Dto");
              result.set(ctx.isAccessibleFromGeneratedPackage(method(dto, "getName")));
            },
            DTO_SOURCE);
    assertThat(compilation).succeeded();
    assertNotNull(result.get());
  }

  @Test
  void performanceTracker_defaultsToNoOp() {
    AtomicReference<Object> tracker = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) -> tracker.set(context(env).getPerformanceTracker()), DTO_SOURCE);
    assertThat(compilation).succeeded();
    assertNotNull(tracker.get());
    assertEquals("NoOpPerformanceTracker", tracker.get().getClass().getSimpleName());
  }

  /**
   * The element-attached and single-argument logging delegates forward to the logger — strict
   * reporting promotes to error, lenient stays a warning.
   */
  @Test
  void loggingDelegates_fullSurface() {
    CapturingProcessingLogger capture = CapturingProcessingLogger.createDebugEnabled();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) -> {
              TypeElement dto = typeElement(env, "test.Dto");
              AnnotationProcessingContext ctx =
                  new AnnotationProcessingContext(capture.logger(), env);
              ctx.info("plain info");
              ctx.debugEndOperation();
              ctx.debugEndOperation("closing %s", "op");
              ctx.warning(dto, "warn %s", "elem");
              ctx.error(dto, "err %s", "elem");
              ctx.reportBasedOnStrictMode(dto, "strict %s", "elem");
              ctx.reportBasedOnStrictMode("strict %s", "flat");
              AnnotationProcessingContext lenient =
                  new AnnotationProcessingContext(
                      capture.logger(),
                      env,
                      AnnotationProcessingOptions.builder().strictModeEnabled(false).build());
              lenient.reportBasedOnStrictMode(dto, "lenient %s", "elem");
              lenient.reportBasedOnStrictMode("lenient %s", "flat");
            },
            DTO_SOURCE);
    assertThat(compilation).succeeded();
    for (String expected :
        List.of(
            "plain info",
            "closing op",
            "warn elem",
            "err elem",
            "strict elem",
            "strict flat",
            "lenient elem",
            "lenient flat")) {
      assertTrue(
          capture.messages().stream().anyMatch(m -> m.contains(expected)),
          () -> "missing log line: " + expected);
    }
  }

  @Test
  void getTypeElement_nullTypeName_returnsNull() {
    AtomicReference<TypeElement> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) -> result.set(context(env).getTypeElement((TypeName) null)),
            DTO_SOURCE);
    assertThat(compilation).succeeded();
    assertNull(result.get());
  }

  /**
   * With no current task, a member that is neither public nor private is not reachable from the
   * generated package — the task's output package is the only grant.
   */
  @Test
  void isAccessibleFromGeneratedPackage_packagePrivateWithoutTask_denied() {
    AtomicReference<Boolean> result = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) -> {
              AnnotationProcessingContext ctx = context(env);
              TypeElement dto = typeElement(env, "test.Base");
              result.set(ctx.isAccessibleFromGeneratedPackage(method(dto, "inherited")));
            },
            DTO_SOURCE);
    assertThat(compilation).succeeded();
    assertEquals(false, result.get());
  }

  /**
   * {@code performanceTrackingEnabled} switches the context to the real tracker writing the report.
   */
  @Test
  void performanceTracker_activeWhenEnabled() {
    AtomicReference<Object> tracker = new AtomicReference<>();
    Compilation compilation =
        CodegenTestUtils.probe(
            (roundEnv, env) ->
                tracker.set(
                    new AnnotationProcessingContext(
                            CapturingProcessingLogger.create().logger(),
                            env,
                            AnnotationProcessingOptions.builder()
                                .performanceTrackingEnabled(true)
                                .phaseHierarchy(
                                    new org.javahelpers.codegen.logging.PhaseHierarchy(
                                        List.of(), Map.of()))
                                .build())
                        .getPerformanceTracker()),
            DTO_SOURCE);
    assertThat(compilation).succeeded();
    assertEquals("ActivePerformanceTracker", tracker.get().getClass().getSimpleName());
  }
}
