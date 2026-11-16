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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.javahelpers.codegen.logging.PhaseHierarchy;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AnnotationProcessingOptions}'s builder. */
class AnnotationProcessingOptionsTest {

  @Test
  void builder_setsAllOptions() {
    PhaseHierarchy hierarchy = new PhaseHierarchy(List.of(), Map.of());
    AnnotationProcessingOptions options =
        AnnotationProcessingOptions.builder()
            .strictModeEnabled(false)
            .performanceTrackingEnabled(true)
            .performanceOutputFile("perf.txt")
            .phaseHierarchy(hierarchy)
            .build();

    assertFalse(options.strictModeEnabled());
    assertTrue(options.performanceTrackingEnabled());
    assertEquals("perf.txt", options.performanceOutputFile());
    assertSame(hierarchy, options.phaseHierarchy());
  }

  @Test
  void builder_defaults() {
    AnnotationProcessingOptions options = AnnotationProcessingOptions.builder().build();

    assertTrue(options.strictModeEnabled());
    assertFalse(options.performanceTrackingEnabled());
    assertNull(options.performanceOutputFile());
  }
}
