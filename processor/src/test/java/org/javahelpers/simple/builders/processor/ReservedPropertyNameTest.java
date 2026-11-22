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
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.forSource;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.simpleBuilderClass;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for setters whose JavaBeans-derived property name is a reserved Java identifier (issue
 * #418). {@code setStatic} derives the property name {@code static}, {@code setDefault} derives
 * {@code default} — both illegal as generated method names. Generation must not fail for the whole
 * target: a matching getter supplies a legal name; otherwise only the offending property is
 * skipped.
 */
class ReservedPropertyNameTest {

  private static final String PACKAGE = "pkg.reserved";

  /**
   * The getter determines the property name independently of the backing field and setter prefix.
   */
  @ParameterizedTest
  @CsvSource({"'', isStatic", "with, withIsStatic"})
  void reservedPropertyUsesGetterName(String prefix, String methodName) {
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "StaticFlagDto",
            """
            private boolean staticValue;
            public boolean getStatic() { return staticValue; }
            public boolean isStatic() { return staticValue; }
            public void setStatic(boolean value) { this.staticValue = value; }
            """);

    Compilation compilation =
        createCompiler().withOptions("-Asimplebuilder.setterPrefix=" + prefix).compile(dto);

    String generated = loadGeneratedSource(compilation, "StaticFlagDtoBuilder");
    assertGenerationSucceeded(compilation, "StaticFlagDtoBuilder", generated);
    assertContaining(
        generated,
        """
        private TrackedValue<Boolean> isStatic = unsetValue();
        """,
        """
        public StaticFlagDtoBuilder %s(boolean isStatic) {
          this.isStatic = changedValue(isStatic);
          return this;
        }
        """
            .formatted(methodName));
  }

  /** A matching backing field cannot rescue a property without a usable getter. */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "public boolean getDefault() { return isDefault; }",
        "private boolean isDefault() { return isDefault; }",
        "public String isDefault() { return null; }",
        "public boolean isDefault(boolean value) { return value; }"
      })
  void reservedPropertyWithoutLegalGetterIsSkipped(String getter) {
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "DefaultFlagDto",
            """
            private boolean isDefault;
            private String name;
            public void setDefault(boolean value) { this.isDefault = value; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            """
                + getter);

    Compilation compilation = createCompiler().compile(dto);

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .hadWarningContaining("No getter yielding a legal name was found")
        .inFile(dto)
        .onLine(9);
    String generated = loadGeneratedSource(compilation, "DefaultFlagDtoBuilder");
    assertContaining(
        generated,
        """
        public DefaultFlagDtoBuilder name(String name) {
          this.name = changedValue(name);
          return this;
        }
        """);
  }

  /**
   * A constructor parameter whose name already matches the getter-derived property must not be
   * duplicated by the deviating setter, and the field must reference the real setter name — {@code
   * setIsStatic} does not exist on the DTO (issue #418).
   */
  @Test
  void constructorPropertyIsNotDuplicatedByDeviatingSetter() {
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "CtorStaticFlagDto",
            """
            private boolean isStatic;
            public CtorStaticFlagDto(boolean isStatic) { this.isStatic = isStatic; }
            public boolean isStatic() { return isStatic; }
            public void setStatic(boolean value) { this.isStatic = value; }
            """);

    Compilation compilation = createCompiler().compile(dto);

    assertThat(compilation).succeeded();
    String generated = loadGeneratedSource(compilation, "CtorStaticFlagDtoBuilder");
    assertGenerationSucceeded(compilation, "CtorStaticFlagDtoBuilder", generated);
    assertContaining(
        generated,
        """
        private TrackedValue<Boolean> isStatic = unsetValue();
        """);
    assertFalse(generated.contains("isStaticBoolean"));
    assertFalse(generated.contains("setIsStatic"));
  }

  /** Strict mode reports the invalid property at its setter and fails compilation. */
  @Test
  void reservedPropertyWithoutGetterFailsInStrictMode() {
    JavaFileObject dto =
        forSource(
            """
            package pkg.reserved;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            @SimpleBuilder
            public class StrictDefaultDto {
              private boolean isDefault;
              public void setDefault(boolean value) { this.isDefault = value; }
            }
            """);

    Compilation compilation =
        createCompiler().withOptions("-Asimplebuilder.strict=true").compile(dto);

    assertThat(compilation).failed();
    assertThat(compilation)
        .hadErrorContaining(
            "Setter 'setDefault' derives property name 'default', which is not a legal Java identifier. "
                + "No getter yielding a legal name was found")
        .inFile(dto)
        .onLine(6);
  }
}
