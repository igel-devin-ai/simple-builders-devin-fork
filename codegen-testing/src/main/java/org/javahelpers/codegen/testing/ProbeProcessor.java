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

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * Test processor that runs a probe once inside a real compilation.
 *
 * <p>During the first processing round the probe receives the {@link RoundEnvironment} and the live
 * {@link ProcessingEnvironment}, so it can look up {@code TypeElement}s of compiled test sources
 * (see {@link ElementTestUtils}) and inspect them with real {@code javax.lang.model} types. Results
 * are captured by the probe and asserted by the test after compilation.
 *
 * <p>{@link #probe(BiFunction, JavaFileObject...)} wraps the whole ceremony — compiler setup,
 * capturing and returning the probe's result — in one call:
 *
 * <pre>{@code
 * TypeElement dto = ProbeProcessor.probe(
 *     (roundEnv, processingEnv) ->
 *         ElementTestUtils.typeElement(processingEnv, "test.Dto"),
 *     CodegenTestUtils.forSource("package test; public class Dto {}"));
 * }</pre>
 */
public class ProbeProcessor extends AbstractProcessor {

  private final BiConsumer<RoundEnvironment, ProcessingEnvironment> probe;
  private boolean ran;

  /**
   * Runs {@code probe} once inside a compilation of {@code sources} and returns its result.
   *
   * @param probe callback invoked in the first processing round
   * @param sources the compilation units to compile
   * @return whatever the probe produced
   */
  @SuppressWarnings(
      "java:S3516") // the result is produced by the probe callback inside the compilation — static
  // analysis sees {@code result} as never mutated
  public static <T> T probe(
      BiFunction<RoundEnvironment, ProcessingEnvironment, T> probe, JavaFileObject... sources) {
    AtomicReference<T> result = new AtomicReference<>();
    CodegenTestUtils.createCompiler(
            new ProbeProcessor(
                (roundEnv, processingEnv) -> result.set(probe.apply(roundEnv, processingEnv))))
        .compile(sources);
    return result.get();
  }

  /**
   * @param probe callback invoked exactly once in the first processing round
   */
  public ProbeProcessor(BiConsumer<RoundEnvironment, ProcessingEnvironment> probe) {
    this.probe = probe;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of("*");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (ran || roundEnv.processingOver()) {
      return false;
    }
    ran = true;
    probe.accept(roundEnv, processingEnv);
    // Claim the annotations of the round the probe consumed.
    return true;
  }
}
