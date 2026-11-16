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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PipelinePhases}. */
class PipelinePhasesTest {

  @Test
  void pipelineHierarchy_containsAllDeclaredPhases() {
    List<String> topLevel = PipelinePhases.PIPELINE_HIERARCHY.topLevelPhases();
    for (String phase :
        List.of(
            PipelinePhases.PHASE_ELEMENT_COLLECTION,
            PipelinePhases.PHASE_CONFIGURATION_RESOLUTION,
            PipelinePhases.PHASE_ANALYSIS,
            PipelinePhases.PHASE_TARGET_MAPPING,
            PipelinePhases.PHASE_CODE_GENERATION)) {
      assertTrue(topLevel.contains(phase), "missing top-level phase " + phase);
    }
  }
}
