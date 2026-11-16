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

package org.javahelpers.codegen.roaster;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;
import org.javahelpers.codegen.testing.TestingGenerationEnvironment;
import org.junit.jupiter.api.Test;

/** Tests for {@link RoasterBackend}'s matched generator/formatter pair. */
class RoasterBackendTest {

  @Test
  void exposesGeneratorAndCachesFormatterPerMode() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    RoasterBackend backend =
        new RoasterBackend(
            TestingGenerationEnvironment.create(capturing.logger()), capturing.logger(), null);

    assertInstanceOf(RoasterCodeGenerator.class, backend.generator());
    RoasterSourceFormatter formatter =
        (RoasterSourceFormatter) backend.formatterFor(FormattingMode.LIGHTWEIGHT);
    // formatters are cached per mode, and distinct across modes
    assertSame(formatter, backend.formatterFor(FormattingMode.LIGHTWEIGHT));
    assertNotSame(formatter, backend.formatterFor(FormattingMode.NONE));
  }
}
