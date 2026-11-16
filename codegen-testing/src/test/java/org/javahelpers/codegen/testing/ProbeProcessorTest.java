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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.testing.compile.Compilation;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.SourceVersion;
import org.junit.jupiter.api.Test;

/** Tests for {@link ProbeProcessor}. */
class ProbeProcessorTest {

  @Test
  void probe_runsOnceWithLiveEnvironment() {
    AtomicInteger invocations = new AtomicInteger();
    AtomicReference<javax.annotation.processing.ProcessingEnvironment> seenEnv =
        new AtomicReference<>();

    Compilation compilation =
        CodegenTestUtils.createCompiler(
                new ProbeProcessor(
                    (roundEnv, processingEnv) -> {
                      invocations.incrementAndGet();
                      seenEnv.set(processingEnv);
                    }))
            .compile(CodegenTestUtils.forSource("package test;\npublic class Dto {}\n"));

    assertThat(compilation).succeeded();
    assertEquals(1, invocations.get());
    assertNotNull(seenEnv.get());
    assertNotNull(seenEnv.get().getElementUtils());
  }

  @Test
  void supportsAllAnnotationsAndLatestSourceVersion() {
    ProbeProcessor processor = new ProbeProcessor((roundEnv, processingEnv) -> {});
    assertEquals(java.util.Set.of("*"), processor.getSupportedAnnotationTypes());
    assertEquals(SourceVersion.latestSupported(), processor.getSupportedSourceVersion());
  }
}
