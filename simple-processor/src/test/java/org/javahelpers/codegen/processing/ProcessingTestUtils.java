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

package org.javahelpers.codegen.processing;

import javax.annotation.processing.ProcessingEnvironment;
import org.javahelpers.codegen.testing.CapturingProcessingLogger;

/** Shared helpers for simple-processor tests running against a live compilation. */
public final class ProcessingTestUtils {

  private ProcessingTestUtils() {
    // Private constructor to prevent instantiation
  }

  /**
   * Creates an {@link AnnotationProcessingContext} backed by a {@link CapturingProcessingLogger}
   * for the given live processing environment.
   *
   * @param processingEnv the processing environment captured inside a probe
   * @return a context ready for analyser/mapper calls
   */
  public static AnnotationProcessingContext context(ProcessingEnvironment processingEnv) {
    return new AnnotationProcessingContext(
        CapturingProcessingLogger.create().logger(), processingEnv);
  }
}
