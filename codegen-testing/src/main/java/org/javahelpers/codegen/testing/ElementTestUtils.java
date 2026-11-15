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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Look-ups for {@code javax.lang.model} elements during a live compilation — used inside {@link
 * ProbeProcessor} probes to grab compiled test types, their fields, methods and parameters.
 */
public final class ElementTestUtils {

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

  /** Finds a field (or any {@link VariableElement} member) of a type by simple name. */
  public static VariableElement field(TypeElement type, String fieldName) {
    return type.getEnclosedElements().stream()
        .filter(VariableElement.class::isInstance)
        .map(VariableElement.class::cast)
        .filter(e -> e.getSimpleName().toString().equals(fieldName))
        .findFirst()
        .orElseThrow();
  }

  /**
   * Finds a method of a type by simple name; fails when several overloads exist — use {@link
   * #method(TypeElement, String, int)} or {@link #parameter(ExecutableElement, int)} then.
   */
  public static ExecutableElement method(TypeElement type, String methodName) {
    return type.getEnclosedElements().stream()
        .filter(ExecutableElement.class::isInstance)
        .map(ExecutableElement.class::cast)
        .filter(e -> e.getSimpleName().toString().equals(methodName))
        .findFirst()
        .orElseThrow();
  }

  /**
   * Finds the {@code overloadIndex}-th method of a type with the given simple name, in declaration
   * order.
   */
  public static ExecutableElement method(TypeElement type, String methodName, int overloadIndex) {
    return type.getEnclosedElements().stream()
        .filter(ExecutableElement.class::isInstance)
        .map(ExecutableElement.class::cast)
        .filter(e -> e.getSimpleName().toString().equals(methodName))
        .skip(overloadIndex)
        .findFirst()
        .orElseThrow();
  }

  /** Returns the {@code index}-th parameter of a method. */
  public static VariableElement parameter(ExecutableElement method, int index) {
    return method.getParameters().get(index);
  }
}
