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

package org.javahelpers.codegen.model.annotation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameSet;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.javahelpers.codegen.model.type.TypeNameWildcard;
import org.junit.jupiter.api.Test;

/** Tests for {@link AnnotationDto#setAnnotationType(TypeName)} validation. */
class AnnotationDtoTest {

  @Test
  void acceptsPlainClassType() {
    AnnotationDto dto = new AnnotationDto();
    assertDoesNotThrow(() -> dto.setAnnotationType(new TypeName("java.lang", "Deprecated")));
    assertDoesNotThrow(() -> dto.setAnnotationType(TypeName.of(Deprecated.class)));
  }

  @Test
  void rejectsNonPlainTypeKinds() {
    TypeName plain = new TypeName("java.lang", "Deprecated");
    assertRejects(TypeNameGeneric.of(java.util.List.class, String.class));
    assertRejects(TypeNameArray.of(String.class));
    assertRejects(TypeNameMap.of(String.class, Integer.class));
    assertRejects(TypeNameSet.of(String.class));
    assertRejects(TypeNameWildcard.extendsBound(plain));
    assertRejects(TypeNamePrimitive.INT);
    assertRejects(new TypeNameVariable("T"));
  }

  private static void assertRejects(TypeName type) {
    AnnotationDto dto = new AnnotationDto();
    assertThrows(IllegalArgumentException.class, () -> dto.setAnnotationType(type));
  }
}
