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

package org.javahelpers.codegen.model.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TypeNameWildcardTest {
  @Test
  void modelsBoundsAndQualifiedNames() {
    TypeNameWildcard unbounded = new TypeNameWildcard();
    assertEquals("?", unbounded.getFullQualifiedName());
    assertEquals(TypeNameWildcard.BoundKind.UNBOUNDED, unbounded.getBoundKind());
    assertTrue(unbounded.getBound().isEmpty());
    TypeNameWildcard upper = TypeNameWildcard.extendsBound(TypeName.of(Number.class));
    assertEquals("? extends java.lang.Number", upper.getFullQualifiedName());
    assertEquals(TypeName.of(Number.class), upper.getBound().orElseThrow());
    TypeNameWildcard lower =
        TypeNameWildcard.superBound(new TypeNameGeneric(List.class, String.class));
    assertEquals("? super java.util.List<java.lang.String>", lower.getFullQualifiedName());
    assertEquals(TypeNameWildcard.BoundKind.SUPER, lower.getBoundKind());
  }

  @Test
  void equalityIncludesDirectionAndBound() {
    TypeNameWildcard upper = TypeNameWildcard.extendsBound(TypeName.of(Number.class));
    TypeNameWildcard same = TypeNameWildcard.extendsBound(TypeName.of(Number.class));
    assertEquals(upper, same);
    assertEquals(upper.hashCode(), same.hashCode());
    assertEquals(new TypeNameWildcard(), new TypeNameWildcard());
    assertNotEquals(upper, new TypeNameWildcard());
    assertNotEquals(upper, TypeNameWildcard.superBound(TypeName.of(Number.class)));
    assertNotEquals(upper, TypeNameWildcard.extendsBound(TypeName.of(Integer.class)));
    assertNotEquals(upper, TypeName.of(Number.class));
    assertNotEquals(upper, null);
  }

  @Test
  void rejectsMissingBound() {
    assertThrows(NullPointerException.class, () -> TypeNameWildcard.extendsBound(null));
    assertThrows(NullPointerException.class, () -> TypeNameWildcard.superBound(null));
  }
}
