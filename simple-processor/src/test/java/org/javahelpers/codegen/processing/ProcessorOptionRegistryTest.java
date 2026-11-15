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

import java.util.List;
import java.util.Map;
import org.javahelpers.codegen.testing.ProcessingEnvironmentStub;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProcessorOptionRegistry}. */
class ProcessorOptionRegistryTest {

  private static CompilerOption option(String name) {
    return new CompilerOption() {
      @Override
      public String getOptionName() {
        return name;
      }

      @Override
      public String getCompilerArgument() {
        return "testdomain." + name;
      }
    };
  }

  @Test
  void supportedOptions_includesOptionAndArgumentNames() {
    ProcessorOptionRegistry registry =
        new ProcessorOptionRegistry(List.of(option("alpha"), option("beta")));
    assertTrue(
        registry
            .supportedOptions()
            .containsAll(List.of("alpha", "testdomain.alpha", "beta", "testdomain.beta")));
  }

  @Test
  void builder_wiresVerboseAndFormatterProfile() {
    ProcessorOptionRegistry registry =
        ProcessorOptionRegistry.builder()
            .options(List.of(option("alpha")))
            .verboseOption(option("verbose"))
            .formatterProfileOption(option("formatter"))
            .build();
    assertTrue(registry.supportedOptions().containsAll(List.of("verbose", "formatter", "alpha")));

    assertTrue(
        registry.isVerboseRequested(
            new CompilerArgumentsReader(
                ProcessingEnvironmentStub.create(Map.of("testdomain.verbose", "true")))));
    assertEquals(
        "compact",
        registry.formatterProfile(
            new CompilerArgumentsReader(
                ProcessingEnvironmentStub.create(Map.of("formatter", " compact ")))));
  }

  @Test
  void defaults_noVerboseAndNoProfile() {
    ProcessorOptionRegistry registry = new ProcessorOptionRegistry(List.of());
    CompilerArgumentsReader reader =
        new CompilerArgumentsReader(ProcessingEnvironmentStub.create(Map.of()));
    assertFalse(registry.isVerboseRequested(reader));
    assertNull(registry.formatterProfile(reader));
  }

  @Test
  void reader_wrapsProcessingEnvironment() {
    ProcessorOptionRegistry registry = new ProcessorOptionRegistry(List.of());
    assertTrue(
        registry
            .reader(ProcessingEnvironmentStub.create(Map.of("verbose", "true")))
            .readBooleanValue(option("verbose")));
  }
}
