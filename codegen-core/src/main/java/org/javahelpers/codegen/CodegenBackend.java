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

package org.javahelpers.codegen;

import org.javahelpers.codegen.enums.FormattingMode;

/**
 * A code-generation backend that bundles the {@link ClassCodeGenerator} and {@link SourceFormatter}
 * into a single unit. This ensures the generator and formatter are always paired correctly — the
 * formatter is designed for the output characteristics of its generator.
 *
 * <p>Implementations cache their instances internally; callers can invoke {@link #generator()} and
 * {@link #formatterFor(FormattingMode)} repeatedly without creating new objects.
 *
 * <p>To plug in a different backend (e.g. JavaPoet), implement this interface and override {@code
 * AbstractCodegenProcessor.createBackend(...)}.
 */
public interface CodegenBackend {

  /**
   * Returns the code generator for this backend.
   *
   * @return the code generator, never {@code null}
   */
  ClassCodeGenerator generator();

  /**
   * Returns the source formatter for the given mode. Implementations typically cache the formatter
   * per mode so that repeated calls with the same mode return the same instance.
   *
   * @param mode the formatting mode
   * @return the source formatter for the mode, never {@code null}
   */
  SourceFormatter formatterFor(FormattingMode mode);
}
