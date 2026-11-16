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

import static org.javahelpers.codegen.testing.ElementTestUtils.field;
import static org.javahelpers.codegen.testing.ElementTestUtils.method;
import static org.javahelpers.codegen.testing.ElementTestUtils.parameter;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  private static <T> T probe(
      java.util.function.BiFunction<
              javax.annotation.processing.RoundEnvironment,
              javax.annotation.processing.ProcessingEnvironment,
              T>
          probeFn) {
    return ProbeProcessor.probe(probeFn, CodegenTestUtils.forSource(DTO_SOURCE));
  }

  @Test
  void typeElement_findsCompiledType() {
    TypeElement dto = probe((roundEnv, env) -> typeElement(env, "test.Dto"));
    assertEquals("test.Dto", dto.getQualifiedName().toString());
  }

  @Test
  void field_findsDeclaredField() {
    VariableElement name = probe((roundEnv, env) -> field(typeElement(env, "test.Dto"), "name"));
    assertEquals("name", name.getSimpleName().toString());
    assertEquals("java.lang.String", name.asType().toString());
  }

  @Test
  void field_failsWhenMissing() {
    // javac wraps processor-thrown exceptions in RuntimeException
    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> probe((roundEnv, env) -> field(typeElement(env, "test.Dto"), "missing")));
    assertTrue(String.valueOf(thrown.getCause()).contains("No field 'missing'"));
  }

  @Test
  void method_failsOnOverloadAmbiguity() {
    // javac wraps processor-thrown exceptions in RuntimeException
    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> probe((roundEnv, env) -> method(typeElement(env, "test.Dto"), "setName")));
    assertTrue(String.valueOf(thrown.getCause()).contains("found 2"));
  }

  @Test
  void method_parameterTypesSelectOverload() {
    ExecutableElement objectOverload =
        probe(
            (roundEnv, env) -> method(typeElement(env, "test.Dto"), "setName", "java.lang.Object"));
    assertEquals("java.lang.Object", objectOverload.getParameters().get(0).asType().toString());
  }

  @Test
  void parameter_returnsIndexedParameter() {
    VariableElement param =
        probe(
            (roundEnv, env) ->
                parameter(method(typeElement(env, "test.Dto"), "setName", "java.lang.String"), 0));
    assertEquals("name", param.getSimpleName().toString());
  }
}
