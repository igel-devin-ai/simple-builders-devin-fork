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
import java.util.function.BiConsumer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Test processor that runs a probe once inside a real compilation.
 *
 * <p>During the first processing round the probe receives the {@link RoundEnvironment} and the live
 * {@link ProcessingEnvironment}, so it can look up {@code TypeElement}s of compiled test sources
 * (see {@link ElementTestUtils}) and inspect them with real {@code javax.lang.model} types. Results
 * are captured by the probe (e.g. into {@link java.util.concurrent.atomic.AtomicReference}s) and
 * asserted by the test after compilation.
 *
 * <pre>{@code
 * AtomicReference<TypeElement> found = new AtomicReference<>();
 * Compilation compilation = CodegenTestUtils.createCompiler(
 *     new ProbeProcessor((roundEnv, processingEnv) ->
 *         found.set(ElementTestUtils.typeElement(processingEnv, "test.Dto"))))
 *     .compile(CodegenTestUtils.forSource("package test; public class Dto {}"));
 * }</pre>
 */
public class ProbeProcessor extends AbstractProcessor {

  private final BiConsumer<RoundEnvironment, ProcessingEnvironment> probe;
  private boolean ran;

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
    return false;
  }
}
