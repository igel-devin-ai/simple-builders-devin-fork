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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.javahelpers.codegen.testing.ProcessingEnvironmentStub;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CompilerArgumentsReader}. */
class CompilerArgumentsReaderTest {

  private static final CompilerOption OPTION =
      new CompilerOption() {
        @Override
        public String getOptionName() {
          return "verbose";
        }

        @Override
        public String getCompilerArgument() {
          return "mydomain.verbose";
        }
      };

  @Test
  void readValue_prefersCompilerArgumentThenBareName() {
    assertEquals("a", CompilerArgumentsReader.readValue(OPTION, Map.of("mydomain.verbose", "a")));
    assertEquals("b", CompilerArgumentsReader.readValue(OPTION, Map.of("verbose", "b")));
    assertNull(CompilerArgumentsReader.readValue(OPTION, Map.of()));
  }

  @Test
  void readValue_usesSystemPropertyFirst() {
    System.setProperty("mydomain.verbose", "sysprop");
    try {
      assertEquals("sysprop", CompilerArgumentsReader.readValue(OPTION, Map.of("verbose", "b")));
    } finally {
      System.clearProperty("mydomain.verbose");
    }
  }

  @Test
  void readValue_readsFromProcessingEnvironment() {
    CompilerArgumentsReader reader =
        new CompilerArgumentsReader(
            ProcessingEnvironmentStub.create(Map.of("mydomain.verbose", "env")));
    assertEquals("env", reader.readValue(OPTION));
  }

  @Test
  void readBooleanValue_parsesAliasesAndDefaults() {
    assertTrue(CompilerArgumentsReader.readBooleanValue(OPTION, Map.of("verbose", "true"), false));
    assertTrue(
        CompilerArgumentsReader.readBooleanValue(OPTION, Map.of("verbose", "enabled"), false));
    assertFalse(CompilerArgumentsReader.readBooleanValue(OPTION, Map.of("verbose", "false"), true));
    assertFalse(
        CompilerArgumentsReader.readBooleanValue(OPTION, Map.of("verbose", "disabled"), true));
    assertTrue(CompilerArgumentsReader.readBooleanValue(OPTION, Map.of(), true));
    assertFalse(CompilerArgumentsReader.readBooleanValue(OPTION, Map.of(), false));
    assertTrue(CompilerArgumentsReader.readBooleanValue(OPTION, Map.of("verbose", ""), true));
    assertFalse(CompilerArgumentsReader.readBooleanValue(OPTION, Map.of("verbose", "junk"), false));
  }

  @Test
  void readBooleanValue_readsFromProcessingEnvironment() {
    CompilerArgumentsReader reader =
        new CompilerArgumentsReader(ProcessingEnvironmentStub.create(Map.of("verbose", "enabled")));
    assertTrue(reader.readBooleanValue(OPTION));
    assertFalse(
        new CompilerArgumentsReader(ProcessingEnvironmentStub.create(Map.of()))
            .readBooleanValue(OPTION));
  }
}
