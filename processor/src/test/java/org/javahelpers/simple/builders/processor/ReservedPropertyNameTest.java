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
import static org.javahelpers.simple.builders.processor.testing.BuilderAsserts.assertGenerationSucceeded;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.createCompiler;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.simpleBuilderClass;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for setters whose JavaBeans-derived property name is a reserved Java identifier (issue
 * #418). {@code setStatic} derives the property name {@code static}, {@code setDefault} derives
 * {@code default} — both illegal as generated method names. Generation must not fail for the whole
 * target: if a backing field with a legal name exists (e.g. {@code isStatic}), its name is used for
 * builder members; otherwise only the offending property is skipped.
 */
class ReservedPropertyNameTest {

  private static final String PACKAGE = "pkg.reserved";

  /**
   * A setter {@code setStatic} with a backing field {@code isStatic} yields a builder whose helpers
   * are named {@code isStatic(...)} while {@code build()} still invokes the real setter {@code
   * setStatic(...)}.
   */
  @Test
  void reservedPropertyName_fallsBackToBackingFieldName() {
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "StaticFlagDto",
            """
            private boolean isStatic;
            private String name;
            public boolean isStatic() { return isStatic; }
            public void setStatic(boolean isStatic) { this.isStatic = isStatic; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    Compilation compilation = createCompiler().compile(dto);

    String generated = loadGeneratedSource(compilation, "StaticFlagDtoBuilder");
    assertGenerationSucceeded(compilation, "StaticFlagDtoBuilder", generated);
    assertContaining(
        generated, "isStatic(boolean isStatic)", "name(String name)", "setStatic", "setName");
  }

  /**
   * A setter {@code setDefault} without a legally named backing field is skipped with a warning;
   * the other properties are still generated and compilation succeeds.
   */
  @Test
  void reservedPropertyName_withoutBackingField_isSkippedWithWarning() {
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "DefaultFlagDto",
            """
            private boolean defaultValue;
            private String name;
            public boolean isDefaultValue() { return defaultValue; }
            public void setDefault(boolean defaultValue) { this.defaultValue = defaultValue; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """);

    Compilation compilation = createCompiler().compile(dto);

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("not a legal Java identifier");
    String generated = loadGeneratedSource(compilation, "DefaultFlagDtoBuilder");
    assertContaining(generated, "name(String name)");
  }

  /**
   * In strict mode the skipped property is reported as an error instead of a warning, consistent
   * with the existing strict diagnostics handling.
   */
  @Test
  void reservedPropertyName_strictMode_isError() {
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "StrictDefaultDto",
            """
            private boolean defaultValue;
            public boolean isDefaultValue() { return defaultValue; }
            public void setDefault(boolean defaultValue) { this.defaultValue = defaultValue; }
            """);

    Compilation compilation =
        createCompiler().withOptions("-Asimplebuilder.strict=true").compile(dto);

    assertThat(compilation).hadErrorContaining("not a legal Java identifier");
  }
}
