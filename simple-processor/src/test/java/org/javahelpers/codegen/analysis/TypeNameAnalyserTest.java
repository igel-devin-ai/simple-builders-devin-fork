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

package org.javahelpers.codegen.analysis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TypeNameAnalyser}. */
class TypeNameAnalyserTest {

  @Test
  void isJavaClass_detectsJavaLangAndUtilTypes() {
    assertTrue(TypeNameAnalyser.isJavaClass(TypeName.of(String.class)));
    assertTrue(TypeNameAnalyser.isJavaClass(TypeName.of(java.util.ArrayList.class)));
    assertFalse(TypeNameAnalyser.isJavaClass(TypeName.of(org.junit.jupiter.api.Test.class)));
  }

  @Test
  void isOptional_matchesJavaUtilOptional() {
    assertTrue(TypeNameAnalyser.isOptional(TypeName.of(Optional.class)));
    assertFalse(TypeNameAnalyser.isOptional(TypeName.of(String.class)));
  }

  @Test
  void isString_matchesJavaLangString() {
    assertTrue(TypeNameAnalyser.isString(TypeName.of(String.class)));
    assertFalse(TypeNameAnalyser.isString(TypeName.of(StringBuilder.class)));
  }

  @Test
  void isOptionalString_requiresParameterizedOptionalOfString() {
    TypeNameGeneric optionalString =
        new TypeNameGeneric("java.util", "Optional", TypeName.of(String.class));
    assertTrue(TypeNameAnalyser.isOptionalString(optionalString));
    TypeNameGeneric optionalInteger =
        new TypeNameGeneric("java.util", "Optional", TypeName.of(Integer.class));
    assertFalse(TypeNameAnalyser.isOptionalString(optionalInteger));
    assertFalse(TypeNameAnalyser.isOptionalString(TypeName.of(String.class)));
  }

  @Test
  void isParameterizedOptional_detectsGenericOptional() {
    TypeNameGeneric optional =
        new TypeNameGeneric("java.util", "Optional", TypeName.of(String.class));
    assertTrue(TypeNameAnalyser.isParameterizedOptional(optional));
    assertFalse(TypeNameAnalyser.isParameterizedOptional(TypeName.of(Optional.class)));
    assertFalse(TypeNameAnalyser.isParameterizedOptional(TypeName.of(String.class)));
  }
}
