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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Writer;
import org.javahelpers.codegen.GeneratedSourceFile;
import org.javahelpers.codegen.logging.NoOpPerformanceTracker;
import org.junit.jupiter.api.Test;

/** Tests for {@link TestingGenerationEnvironment}. */
class TestingGenerationEnvironmentTest {

  @Test
  void create_capturesWrittenSourceInMemory() throws Exception {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    TestingGenerationEnvironment env = TestingGenerationEnvironment.create(capturing.logger());

    GeneratedSourceFile file = env.createSourceFile("com.example.Generated");
    try (Writer writer = file.openWriter()) {
      writer.write("public class Generated {}");
    }

    assertEquals("public class Generated {}", env.generatedSource());
    assertEquals("mem:///com/example/Generated.java", env.writtenUri().toString());
  }

  @Test
  void createUnwritable_returnsNullSourceFile() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    TestingGenerationEnvironment env =
        TestingGenerationEnvironment.createUnwritable(capturing.logger());

    assertNull(env.createSourceFile("com.example.Generated"));
  }

  @Test
  void exposesLoggerNoOpTrackerAndReporter() {
    CapturingProcessingLogger capturing = CapturingProcessingLogger.create();
    TestingGenerationEnvironment env = TestingGenerationEnvironment.create(capturing.logger());

    assertSame(capturing.logger(), env.getLogger());
    assertInstanceOf(NoOpPerformanceTracker.class, env.getPerformanceTracker());

    env.getIssueReporter().report("issue %s", "one");
    assertTrue(
        capturing.messages().stream().anyMatch(m -> m.contains("issue one")),
        "issue reporter routes to the logger as warning");
  }
}
