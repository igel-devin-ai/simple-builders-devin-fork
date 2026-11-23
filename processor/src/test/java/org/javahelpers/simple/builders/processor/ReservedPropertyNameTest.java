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
import java.util.List;
import java.util.stream.Stream;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  /** An ignored getter cannot rescue an illegal property name. */
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void ignoredGetterCannotNameReservedProperty(boolean strict) {
    JavaFileObject dto =
        forSource(
            """
            package pkg.reserved;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            import org.javahelpers.simple.builders.core.annotations.IgnoreInBuilder;
            @SimpleBuilder
            public class IgnoredGetterDto {
              @IgnoreInBuilder
              public boolean isStatic() { return false; }
              public void setStatic(boolean value) {}
            }
            """);
    Compilation compilation =
        createCompiler().withOptions("-Asimplebuilder.strict=" + strict).compile(dto);
    String message =
        "Setter 'setStatic' derives property name 'static', which is not a legal Java identifier. "
            + "No getter yielding a legal name was found";
    if (strict) {
      assertThat(compilation).failed();
      assertThat(compilation).hadErrorContaining(message).inFile(dto).onLine(8);
    } else {
      assertThat(compilation).succeeded();
      assertThat(compilation).hadWarningContaining(message).inFile(dto).onLine(8);
      String generated = loadGeneratedSource(compilation, "IgnoredGetterDtoBuilder");
      assertContaining(
          generated,
          """
          public IgnoredGetterDto build() {
            IgnoredGetterDto result = new IgnoredGetterDto();
            return result;
          }
          """);
      assertFalse(generated.contains("private TrackedValue<Boolean> isStatic = unsetValue();"));
    }
  }

  /** Getter preference skips ignored candidates before choosing a legal property name. */
  @Test
  void reservedPropertyUsesNextEligibleGetter() {
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "AlternateGetterDto",
            """
            @org.javahelpers.simple.builders.core.annotations.IgnoreInBuilder
            public boolean isStatic() { return false; }
            public boolean getSTATIC() { return false; }
            public void setStatic(boolean value) {}
            """);
    Compilation compilation = createCompiler().compile(dto);
    String generated = loadGeneratedSource(compilation, "AlternateGetterDtoBuilder");
    assertGenerationSucceeded(compilation, "AlternateGetterDtoBuilder", generated);
    assertContaining(
        generated,
        """
        public AlternateGetterDtoBuilder sTATIC(boolean sTATIC) {
          this.sTATIC = changedValue(sTATIC);
          return this;
        }
        """);
    assertContaining(
        generated,
        """
        public AlternateGetterDto build() {
          if (this.sTATIC.isSet() && this.sTATIC.value() == null) {
            throw new IllegalStateException("Field 'sTATIC' is marked as non-null but null value was provided");
          }
          AlternateGetterDto result = new AlternateGetterDto();
          this.sTATIC.ifSet(result::setStatic);
          return result;
        }
        """);
  }

  /** Only setter-shaped void methods become properties, including getter-rescued setStatic. */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "public void withIgnored(String value) {}",
        "public void ignored(String value) {}",
        "public void setignored(String value) {}",
        "public String setIgnored(String value) { return value; }"
      })
  void nonSetterMethodsDoNotCreateProperties(String otherMethod) {
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "SetterShapeDto",
            """
        public boolean isStatic() { return false; }
        public void setStatic(boolean value) {}
        """
                + otherMethod);
    Compilation compilation = createCompiler().compile(dto);
    String generated = loadGeneratedSource(compilation, "SetterShapeDtoBuilder");
    assertGenerationSucceeded(compilation, "SetterShapeDtoBuilder", generated);
    assertContaining(
        generated,
        """
        public SetterShapeDto build() {
          if (this.isStatic.isSet() && this.isStatic.value() == null) {
            throw new IllegalStateException("Field 'isStatic' is marked as non-null but null value was provided");
          }
          SetterShapeDto result = new SetterShapeDto();
          this.isStatic.ifSet(result::setStatic);
          return result;
        }
        """);
    assertFalse(generated.contains("private TrackedValue<String> ignored = unsetValue();"));
  }

  /**
   * The getter determines the property name independently of the backing field and setter prefix.
   */
  @ParameterizedTest
  @MethodSource("reservedPropertyNames")
  void reservedPropertyUsesGetterName(String word, String prefix) {
    String title = Character.toUpperCase(word.charAt(0)) + word.substring(1);
    String property = "is" + title;
    String methodName = prefix.isEmpty() ? property : prefix + "Is" + title;
    JavaFileObject dto =
        simpleBuilderClass(
            PACKAGE,
            "ReservedFlagDto",
            """
            private boolean value;
            %2$s
            public boolean is%1$s() { return value; }
            public void set%1$s(boolean value) { this.value = value; }
            """
                .formatted(
                    title,
                    word.equals("class")
                        ? ""
                        : "public boolean get%s() { return value; }".formatted(title)));

    Compilation compilation =
        createCompiler().withOptions("-Asimplebuilder.setterPrefix=" + prefix).compile(dto);

    String generated = loadGeneratedSource(compilation, "ReservedFlagDtoBuilder");
    assertGenerationSucceeded(compilation, "ReservedFlagDtoBuilder", generated);
    assertContaining(
        generated,
        "private TrackedValue<Boolean> %s = unsetValue();".formatted(property),
        """
        public ReservedFlagDtoBuilder %1$s(boolean %2$s) {
          this.%2$s = changedValue(%2$s);
          return this;
        }
        """
            .formatted(methodName, property),
        """
        public ReservedFlagDto build() {
          if (this.%1$s.isSet() && this.%1$s.value() == null) {
            throw new IllegalStateException("Field '%1$s' is marked as non-null but null value was provided");
          }
          ReservedFlagDto result = new ReservedFlagDto();
          this.%1$s.ifSet(result::set%2$s);
          return result;
        }
        """
            .formatted(property, title));
  }

  @ParameterizedTest
  @MethodSource("reservedWords")
  void reservedPropertyWithoutGetterIsSkipped(String word) {
    String title = Character.toUpperCase(word.charAt(0)) + word.substring(1);
    JavaFileObject dto =
        forSource(
            """
            package pkg.reserved;
            import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
            @SimpleBuilder
            public class ReservedFlagDto {
              public void set%s(boolean value) {}
            }
            """
                .formatted(title));
    Compilation compilation = createCompiler().compile(dto);
    assertThat(compilation).succeeded();
    assertThat(compilation).hadErrorCount(0);
    assertThat(compilation).hadWarningCount(1);
    assertThat(compilation)
        .hadWarningContaining(
            "Setter 'set%s' derives property name '%s', which is not a legal Java identifier. "
                    .formatted(title, word)
                + "No getter yielding a legal name was found")
        .inFile(dto)
        .onLine(5);
    String generated = loadGeneratedSource(compilation, "ReservedFlagDtoBuilder");
    assertContaining(
        generated,
        """
        public ReservedFlagDto build() {
          ReservedFlagDto result = new ReservedFlagDto();
          return result;
        }
        """);
    assertFalse(generated.contains("TrackedValue<"));
  }

  private static Stream<Arguments> reservedPropertyNames() {
    return reservedWords()
        .flatMap(word -> Stream.of("", "with").map(prefix -> Arguments.of(word, prefix)));
  }

  private static Stream<String> reservedWords() {
    return Stream.of(
            ("abstract assert boolean break byte case catch char class const continue default do "
                    + "double else enum extends final finally float for goto if implements import "
                    + "instanceof int interface long native new package private protected public "
                    + "return short static strictfp super switch synchronized this throw throws "
                    + "transient try void volatile while _ true false null")
                .split(" "))
        .filter(word -> !word.equals("_"))
        .filter(
            word ->
                SourceVersion.isKeyword(word) || List.of("true", "false", "null").contains(word));
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
