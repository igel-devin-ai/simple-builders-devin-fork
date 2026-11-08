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
import org.javahelpers.codegen.CodegenBackend;
import org.javahelpers.codegen.GenerationIssueReporter;
import org.javahelpers.codegen.logging.PerformanceTracker;
import org.javahelpers.codegen.logging.ProcessingLogger;
import org.javahelpers.codegen.roaster.RoasterBackend;

/**
 * Resolves the {@link CodegenBackend} to use for code generation. Currently returns the Roaster
 * backend directly.
 *
 * <p>In a future iteration (see <a
 * href="https://github.com/java-helpers/simple-builders/issues/366">#366</a>) this resolver will
 * probe the classpath and select the best available backend in priority order: Roaster (if
 * available) → JavaPoet (if available) → built-in lightweight fallback.
 */
final class CodegenBackendResolver {

  private CodegenBackendResolver() {}

  /**
   * Resolves the code-generation backend for the given environment.
   *
   * @param processingEnv the annotation processing environment
   * @param logger the processing logger
   * @param tracker the performance tracker
   * @param reporter the reporter for problems found during generation
   * @param formatterProfile the configured Eclipse formatter profile location, or {@code null}
   * @return the resolved backend
   */
  static CodegenBackend resolve(
      ProcessingEnvironment processingEnv,
      ProcessingLogger logger,
      PerformanceTracker tracker,
      GenerationIssueReporter reporter,
      String formatterProfile) {
    // For now: direct Roaster dependency.
    // #366: classpath detection (Roaster → JavaPoet → built-in lightweight)
    return new RoasterBackend(
        new AnnotationProcessingEnvironment(processingEnv, logger, tracker, reporter),
        logger,
        formatterProfile);
  }
}
