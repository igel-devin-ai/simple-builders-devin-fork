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

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import org.javahelpers.codegen.GeneratedSourceFile;
import org.javahelpers.codegen.GenerationEnvironment;
import org.javahelpers.codegen.GenerationIssueReporter;
import org.javahelpers.codegen.logging.NoOpPerformanceTracker;
import org.javahelpers.codegen.logging.PerformanceTracker;
import org.javahelpers.codegen.logging.ProcessingLogger;

/**
 * A {@link GenerationEnvironment} built only from codegen-core types for tests of code generators:
 * issues surface on the logger as warnings, timing is not tracked, and generated sources are
 * captured in memory for assertions.
 *
 * <pre>{@code
 * TestingGenerationEnvironment env = TestingGenerationEnvironment.create(logger);
 * generator.generateClass(classDef, formatter);
 * String source = env.generatedSource();
 * }</pre>
 */
public final class TestingGenerationEnvironment implements GenerationEnvironment {

  private final ProcessingLogger logger;
  private final boolean writable;
  private InMemorySourceFile writtenFile;

  private TestingGenerationEnvironment(ProcessingLogger logger, boolean writable) {
    this.logger = logger;
    this.writable = writable;
  }

  /** Environment whose {@code createSourceFile} captures the emitted source in memory. */
  public static TestingGenerationEnvironment create(ProcessingLogger logger) {
    return new TestingGenerationEnvironment(logger, true);
  }

  /**
   * Environment with a default {@link CapturingProcessingLogger} whose {@code createSourceFile}
   * captures the emitted source in memory.
   */
  public static TestingGenerationEnvironment create() {
    return new TestingGenerationEnvironment(CapturingProcessingLogger.create().logger(), true);
  }

  /**
   * Environment whose {@code createSourceFile} returns {@code null} — the file-write step fails
   * deterministically on it.
   */
  public static TestingGenerationEnvironment createUnwritable(ProcessingLogger logger) {
    return new TestingGenerationEnvironment(logger, false);
  }

  @Override
  public ProcessingLogger getLogger() {
    return logger;
  }

  @Override
  public PerformanceTracker getPerformanceTracker() {
    return new NoOpPerformanceTracker();
  }

  @Override
  public GenerationIssueReporter getIssueReporter() {
    return logger::warning;
  }

  @Override
  public GeneratedSourceFile createSourceFile(CharSequence qualifiedName) {
    if (!writable) {
      return null;
    }
    writtenFile = new InMemorySourceFile(qualifiedName);
    return writtenFile;
  }

  /** The source text written to the captured file. */
  public String generatedSource() {
    return writtenFile.buffer.toString();
  }

  /** The URI the captured file was written to ({@code mem:///pkg/Name.java}). */
  public URI writtenUri() {
    return writtenFile.toUri();
  }

  private static final class InMemorySourceFile implements GeneratedSourceFile {
    private final String name;
    private final StringWriter buffer = new StringWriter();

    private InMemorySourceFile(CharSequence qualifiedName) {
      this.name = qualifiedName.toString();
    }

    @Override
    public URI toUri() {
      return URI.create("mem:///" + name.replace('.', '/') + ".java");
    }

    @Override
    public Writer openWriter() {
      return buffer;
    }
  }
}
