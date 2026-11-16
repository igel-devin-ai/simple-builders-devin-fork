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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.javahelpers.codegen.model.type.TypeName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link GeneratedTypes}. */
class GeneratedTypesTest {

  @Test
  void add_registersGeneratedTypeAndFindsIt() {
    GeneratedTypes types = new GeneratedTypes();
    TypeName source = TypeName.of(String.class);
    TypeName generated = TypeName.of(StringBuilder.class);
    assertTrue(types.add(source, generated));
    assertEquals(generated, types.findGenerated(source).orElseThrow());
  }

  @Test
  void add_rejectsDuplicateTarget() {
    GeneratedTypes types = new GeneratedTypes();
    TypeName source = TypeName.of(String.class);
    assertTrue(types.add(source, TypeName.of(StringBuilder.class)));
    assertFalse(types.add(source, TypeName.of(StringBuffer.class)));
  }

  @Test
  void findGenerated_emptyWhenUnknown() {
    assertTrue(new GeneratedTypes().findGenerated(TypeName.of(String.class)).isEmpty());
  }

  @Test
  void clear_removesAllMappings() {
    GeneratedTypes types = new GeneratedTypes();
    types.add(TypeName.of(String.class), TypeName.of(StringBuilder.class));
    types.clear();
    assertTrue(types.findGenerated(TypeName.of(String.class)).isEmpty());
  }
}
