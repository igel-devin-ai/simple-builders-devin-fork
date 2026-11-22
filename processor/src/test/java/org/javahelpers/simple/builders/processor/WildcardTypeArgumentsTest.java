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

package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.codegen.testing.ProcessorAsserts.assertContaining;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Wildcards survive mapping and compilation without unsafe element-writing helpers. */
class WildcardTypeArgumentsTest {
  @ParameterizedTest
  @ValueSource(
      strings = {
        "List<?>",
        "List<? extends Number>",
        "List<? super Integer>",
        "List<Foo<?>>",
        "Set<? extends Number>",
        "Map<String, ? super Integer>",
        "Optional<? extends Number>",
        "ArrayList<? extends Number>"
      })
  void preservesWildcardSignatures(String type) {
    Compilation compilation =
        createCompiler()
            .withOptions("-Asimplebuilder.strict=true")
            .compile(
                JavaFileObjects.forSourceString("wild.Foo", "package wild; public class Foo<T> {}"),
                JavaFileObjects.forSourceString(
                    "wild.WildcardDto",
                    """
            package wild;
            import java.util.*;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            @SimpleBuilder
            public class WildcardDto {
              private %s values;
              public %s getValues() { return values; }
              public void setValues(%s values) { this.values = values; }
            }
            """
                        .formatted(type, type, type)));
    assertThat(compilation).succeeded();
    String source = loadGeneratedSource(compilation, "WildcardDtoBuilder");
    assertContaining(
        source,
        """
        private TrackedValue<%s> values = unsetValue();
        """
            .formatted(type),
        """
        public WildcardDtoBuilder values(%s values) {
          this.values = changedValue(values);
          return this;
        }
        """
            .formatted(type));
  }
}
