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

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Look-ups for {@code javax.lang.model} elements during a live compilation — used inside {@link
 * ProbeProcessor} probes to grab compiled test types, their fields, methods and parameters.
 */
public final class ElementTestUtils {

  private static final String ON = "' on ";

  private ElementTestUtils() {}

  /**
   * Resolves a compiled type by qualified name.
   *
   * @param processingEnv the live processing environment
   * @param qualifiedName fully qualified type name (e.g. {@code "test.Dto"})
   */
  public static TypeElement typeElement(ProcessingEnvironment processingEnv, String qualifiedName) {
    return processingEnv.getElementUtils().getTypeElement(qualifiedName);
  }

  /**
   * Finds the field of a type by simple name. Only fields are considered — parameters belong to
   * methods, not to the type. Fails when the field is missing or declared several times.
   */
  public static VariableElement field(TypeElement type, String fieldName) {
    return exactlyOne(
        type.getEnclosedElements().stream()
            .filter(VariableElement.class::isInstance)
            .map(VariableElement.class::cast)
            .filter(e -> e.getSimpleName().toString().equals(fieldName)),
        "field '" + fieldName + ON + type.getQualifiedName());
  }

  /**
   * Finds the method of a type by simple name; fails when it is missing or overloaded — use {@link
   * #method(TypeElement, String, String...)} to select an overload by its parameter types.
   */
  public static ExecutableElement method(TypeElement type, String methodName) {
    return exactlyOne(
        type.getEnclosedElements().stream()
            .filter(ExecutableElement.class::isInstance)
            .map(ExecutableElement.class::cast)
            .filter(e -> e.getSimpleName().toString().equals(methodName)),
        "method '" + methodName + ON + type.getQualifiedName());
  }

  /**
   * Finds the method of a type by simple name and parameter types (as qualified names, e.g. {@code
   * "java.lang.String"}), matching each parameter's erased type string.
   */
  public static ExecutableElement method(
      TypeElement type, String methodName, String... parameterTypes) {
    return exactlyOne(
        type.getEnclosedElements().stream()
            .filter(ExecutableElement.class::isInstance)
            .map(ExecutableElement.class::cast)
            .filter(e -> e.getSimpleName().toString().equals(methodName))
            .filter(
                e ->
                    e.getParameters().stream()
                        .map(VariableElement::asType)
                        .map(TypeMirror::toString)
                        .toList()
                        .equals(List.of(parameterTypes))),
        "method '"
            + methodName
            + "("
            + String.join(", ", parameterTypes)
            + ON
            + type.getQualifiedName());
  }

  private static <T> T exactlyOne(Stream<T> matches, String description) {
    List<T> found = matches.toList();
    if (found.isEmpty()) {
      throw new IllegalArgumentException("No " + description + " found");
    }
    if (found.size() > 1) {
      throw new IllegalArgumentException(
          "Expected one " + description + " but found " + found.size());
    }
    return found.get(0);
  }

  /** Returns the {@code index}-th parameter of a method. */
  public static VariableElement parameter(ExecutableElement method, int index) {
    return method.getParameters().get(index);
  }
}
