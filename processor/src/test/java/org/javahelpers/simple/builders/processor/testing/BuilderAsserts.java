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

package org.javahelpers.simple.builders.processor.testing;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.codegen.testing.ProcessorAsserts.assertContaining;
import static org.javahelpers.codegen.testing.ProcessorAsserts.assertNoSourceGenerated;

import com.google.testing.compile.Compilation;
import org.apache.commons.lang3.Strings;
import org.javahelpers.codegen.testing.ProcessorAsserts;

/**
 * Assertion helpers for generated builders. The assertions which do not know about builders live in
 * {@link ProcessorAsserts}.
 */
public final class BuilderAsserts {

  private static final String BUILDER_SUFFIX = "Builder";

  private BuilderAsserts() {}

  /**
   * Asserts compilation succeeded and that the basic builder API exists in the provided generated
   * source (build() and static create()).
   */
  public static void assertGenerationSucceeded(
      Compilation compilation, String builderSimpleName, String generated) {
    assertThat(compilation).succeededWithoutWarnings();

    String targetSimpleName = Strings.CS.removeEnd(builderSimpleName, BUILDER_SUFFIX);
    assertContaining(
        generated,
        "public " + targetSimpleName + " build()",
        "public static " + builderSimpleName + " create()");
  }

  /**
   * Asserts that no generated source file for the given class' builder exists in the compilation.
   *
   * <p>The check is based on the simple class name: a file whose name ends with {@code
   * <className>Builder.java} is considered the builder for {@code className}.
   *
   * @param compilation the compilation result
   * @param className the simple class name for which no builder should have been generated
   * @param message the failure message
   */
  public static void assertNoBuilderGenerated(
      Compilation compilation, String className, String message) {
    assertNoSourceGenerated(compilation, className + BUILDER_SUFFIX, message);
  }
}
