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

import java.util.EnumMap;
import org.javahelpers.codegen.ClassCodeGenerator;
import org.javahelpers.codegen.CodegenBackend;
import org.javahelpers.codegen.GenerationEnvironment;
import org.javahelpers.codegen.SourceFormatter;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.logging.ProcessingLogger;

/**
 * {@link CodegenBackend} backed by the Roaster engine. Bundles the {@link RoasterCodeGenerator} and
 * {@link RoasterSourceFormatter} so they are always used as a matched pair.
 *
 * <p>The formatter is specifically designed for the output characteristics of the Roaster code
 * generator — in particular, its {@link FormattingMode#LIGHTWEIGHT LIGHTWEIGHT} mode repairs
 * Roaster-specific output quirks (concatenated imports, glued braces, malformed Javadoc). Using
 * this formatter with a different generator's output may produce incorrect results.
 *
 * <p>When a drop-in backend mechanism is introduced later, this class will be discovered through a
 * registry or configuration-driven lookup; until then it is the built-in default.
 */
public final class RoasterBackend implements CodegenBackend {

  private final ClassCodeGenerator generator;
  private final ProcessingLogger logger;
  private final String formatterProfile;
  private final EnumMap<FormattingMode, SourceFormatter> formatters =
      new EnumMap<>(FormattingMode.class);

  public RoasterBackend(
      GenerationEnvironment environment, ProcessingLogger logger, String formatterProfile) {
    this.generator = new RoasterCodeGenerator(environment);
    this.logger = logger;
    this.formatterProfile = formatterProfile;
  }

  @Override
  public ClassCodeGenerator generator() {
    return generator;
  }

  @Override
  public SourceFormatter formatterFor(FormattingMode mode) {
    return formatters.computeIfAbsent(
        mode, m -> new RoasterSourceFormatter(logger, m, formatterProfile));
  }
}
