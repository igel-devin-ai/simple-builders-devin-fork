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

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import org.javahelpers.codegen.processing.AnnotationProcessingContext;

/**
 * Shared helpers for simple-processor tests running against a live compilation. Lives in this
 * module's test sources (not codegen-testing) because {@link AnnotationProcessingContext} is a
 * simple-processor type — codegen-testing sits below it in the dependency graph.
 */
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

  /**
   * Compiles the given source texts with a {@link ProbeProcessor} running {@code probe} in the
   * first processing round and asserts the compilation succeeded.
   *
   * @param probe callback receiving the {@link RoundEnvironment} and {@link ProcessingEnvironment}
   * @param sources the Java source texts to compile
   * @return the compilation result
   */
  public static Compilation probe(
      BiConsumer<RoundEnvironment, ProcessingEnvironment> probe, String... sources) {
    Compilation compilation = CodegenTestUtils.probe(probe, sources);
    assertThat(compilation).succeeded();
    return compilation;
  }

  /**
   * Same as {@link #probe(BiConsumer, String...)} for source lists.
   *
   * @param probe callback receiving the {@link RoundEnvironment} and {@link ProcessingEnvironment}
   * @param sources the Java source texts to compile
   * @return the compilation result
   */
  public static Compilation probe(
      BiConsumer<RoundEnvironment, ProcessingEnvironment> probe, List<String> sources) {
    Compilation compilation = CodegenTestUtils.probe(probe, sources);
    assertThat(compilation).succeeded();
    return compilation;
  }
}
