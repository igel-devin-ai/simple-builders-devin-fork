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

package org.javahelpers.codegen.testing;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.codegen.testing.ElementTestUtils.field;
import static org.javahelpers.codegen.testing.ElementTestUtils.method;
import static org.javahelpers.codegen.testing.ElementTestUtils.parameter;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.testing.compile.Compilation;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.junit.jupiter.api.Test;

/** Tests for {@link ElementTestUtils} against elements captured from a real compilation. */
class ElementTestUtilsTest {

  private static final String DTO_SOURCE =
      """
      package test;
      public class Dto {
        private String name;
        public void setName(String name) { this.name = name; }
        public void setName(Object name) {}
      }
      """;

  private static Compilation compile(
      java.util.function.BiConsumer<
              javax.annotation.processing.RoundEnvironment,
              javax.annotation.processing.ProcessingEnvironment>
          probe) {
    return CodegenTestUtils.createCompiler(new ProbeProcessor(probe))
        .compile(CodegenTestUtils.forSource(DTO_SOURCE));
  }

  @Test
  void typeElement_findsCompiledType() {
    AtomicReference<TypeElement> result = new AtomicReference<>();
    Compilation compilation = compile((roundEnv, env) -> result.set(typeElement(env, "test.Dto")));

    assertThat(compilation).succeeded();
    assertEquals("test.Dto", result.get().getQualifiedName().toString());
  }

  @Test
  void field_findsDeclaredField() {
    AtomicReference<VariableElement> result = new AtomicReference<>();
    Compilation compilation =
        compile((roundEnv, env) -> result.set(field(typeElement(env, "test.Dto"), "name")));

    assertThat(compilation).succeeded();
    assertEquals("name", result.get().getSimpleName().toString());
    assertEquals("java.lang.String", result.get().asType().toString());
  }

  @Test
  void method_returnsFirstOverload() {
    AtomicReference<ExecutableElement> result = new AtomicReference<>();
    Compilation compilation =
        compile((roundEnv, env) -> result.set(method(typeElement(env, "test.Dto"), "setName")));

    assertThat(compilation).succeeded();
    assertEquals("setName", result.get().getSimpleName().toString());
  }

  @Test
  void method_overloadIndexSelectsLaterOverload() {
    AtomicReference<ExecutableElement> result = new AtomicReference<>();
    Compilation compilation =
        compile((roundEnv, env) -> result.set(method(typeElement(env, "test.Dto"), "setName", 1)));

    assertThat(compilation).succeeded();
    assertEquals("java.lang.Object", result.get().getParameters().get(0).asType().toString());
  }

  @Test
  void parameter_returnsIndexedParameter() {
    AtomicReference<VariableElement> result = new AtomicReference<>();
    Compilation compilation =
        compile(
            (roundEnv, env) -> {
              ExecutableElement mth = method(typeElement(env, "test.Dto"), "setName");
              result.set(parameter(mth, 0));
            });

    assertThat(compilation).succeeded();
    assertEquals("name", result.get().getSimpleName().toString());
  }
}
