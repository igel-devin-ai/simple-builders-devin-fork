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

package org.javahelpers.codegen.analysis;

import static org.javahelpers.codegen.testing.ElementTestUtils.field;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.javahelpers.codegen.testing.ProcessingTestUtils.context;
import static org.javahelpers.codegen.testing.ProcessingTestUtils.probe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link FieldAnnotationExtractor} against elements captured from a compilation. */
class FieldAnnotationExtractorTest {

  private static final String DTO_SOURCE =
      """
      package test;
      public class Dto {
        @Deprecated
        private String legacy;
        @test.Default("GENERAL")
        private String mode;
        @test.NonNull
        private String required;
        private @test.NonNull String typed;
        @test.Rich(tags = {"alpha", "beta"}, type = String.class, cond = test.ItemCondition.BAD)
        private String rich;
        @test.Rich(tags = {})
        private String richEmpty;
        private String plain;
      }
      """;

  private static final String DEFAULT_ANNOTATION_SOURCE =
      """
      package test;
      public @interface Default { String value(); }
      """;

  private static final String NONNULL_ANNOTATION_SOURCE =
      """
      package test;
      @java.lang.annotation.Target({
        java.lang.annotation.ElementType.FIELD,
        java.lang.annotation.ElementType.TYPE_USE
      })
      public @interface NonNull {}
      """;

  private static final String ITEM_CONDITION_SOURCE =
      """
      package test;
      public enum ItemCondition { GOOD, BAD }
      """;

  private static final String RICH_ANNOTATION_SOURCE =
      """
      package test;
      public @interface Rich {
        String[] tags() default {};
        Class<?> type() default Void.class;
        ItemCondition cond() default ItemCondition.GOOD;
      }
      """;

  private static final List<String> DTO_SOURCES =
      List.of(
          DTO_SOURCE,
          DEFAULT_ANNOTATION_SOURCE,
          NONNULL_ANNOTATION_SOURCE,
          ITEM_CONDITION_SOURCE,
          RICH_ANNOTATION_SOURCE);

  @Test
  void extractDeprecatedAnnotation_findsDeprecatedField() {
    AtomicReference<Optional<AnnotationDto>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractDeprecatedAnnotation(
                    field(typeElement(processingEnv, "test.Dto"), "legacy"),
                    context(processingEnv))),
        DTO_SOURCES);
    assertTrue(result.get().isPresent());
    assertEquals("java.lang.Deprecated", result.get().get().getAnnotationType().toString());
  }

  @Test
  void extractDeprecatedAnnotation_emptyForPlainField() {
    AtomicReference<Optional<AnnotationDto>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractDeprecatedAnnotation(
                    field(typeElement(processingEnv, "test.Dto"), "plain"),
                    context(processingEnv))),
        DTO_SOURCES);
    assertTrue(result.get().isEmpty());
  }

  @Test
  void extractAnnotations_returnsMemberValues() {
    AtomicReference<List<AnnotationDto>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractAnnotations(
                    field(typeElement(processingEnv, "test.Dto"), "mode"), context(processingEnv))),
        DTO_SOURCES);
    assertEquals(1, result.get().size());
    AnnotationDto annotation = result.get().get(0);
    assertEquals("Default", annotation.getAnnotationType().getClassName());
    assertEquals("\"GENERAL\"", annotation.getMembers().get("value"));
  }

  @Test
  void hasNonNullConstraint_detectsNonNullAnnotation() {
    AtomicReference<Boolean> nonNull = new AtomicReference<>();
    AtomicReference<Boolean> plain = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          var dto = typeElement(processingEnv, "test.Dto");
          nonNull.set(FieldAnnotationExtractor.hasNonNullConstraint(field(dto, "required")));
          plain.set(FieldAnnotationExtractor.hasNonNullConstraint(field(dto, "plain")));
        },
        DTO_SOURCES);
    assertTrue(nonNull.get());
    assertFalse(plain.get());
  }

  @Test
  void extractAnnotationValue_matchesAnnotationBySimpleName() {
    AtomicReference<Optional<String>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractAnnotationValue(
                    field(typeElement(processingEnv, "test.Dto"), "mode"), Set.of("Default"))),
        DTO_SOURCES);
    assertEquals(Optional.of("GENERAL"), result.get());
  }

  @Test
  void formatDefaultExpression_quotesStringValues() {
    assertEquals(
        "\"GENERAL\"",
        FieldAnnotationExtractor.formatDefaultExpression("GENERAL", TypeName.of(String.class)));
  }

  @Test
  void formatDefaultExpression_quotesCharValues() {
    assertEquals(
        "'A'",
        FieldAnnotationExtractor.formatDefaultExpression(
            "A", org.javahelpers.codegen.model.type.TypeNamePrimitive.CHAR));
  }

  @Test
  void formatDefaultExpression_qualifiesSimpleEnumConstant() {
    TypeName enumType = new TypeName("test", "ItemCondition");
    enumType.setEnumType(true);
    assertEquals(
        "ItemCondition.GOOD", FieldAnnotationExtractor.formatDefaultExpression("GOOD", enumType));
  }

  @Test
  void formatDefaultExpression_keepsComplexExpressions() {
    TypeName enumType = new TypeName("test", "ItemCondition");
    enumType.setEnumType(true);
    assertEquals(
        "ItemCondition.GOOD",
        FieldAnnotationExtractor.formatDefaultExpression("ItemCondition.GOOD", enumType));
    assertEquals(
        "List.of()",
        FieldAnnotationExtractor.formatDefaultExpression("List.of()", TypeName.of(List.class)));
  }

  @Test
  void extractDeprecatedAnnotation_nullElement_returnsEmpty() {
    assertTrue(FieldAnnotationExtractor.extractDeprecatedAnnotation(null, null).isEmpty());
  }

  @Test
  void extractDeprecatedAnnotation_ignoresOtherAnnotations() {
    // 'mode' carries @Default but not @Deprecated — the mirror scan must fall through
    AtomicReference<Optional<AnnotationDto>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractDeprecatedAnnotation(
                    field(typeElement(processingEnv, "test.Dto"), "mode"), context(processingEnv))),
        DTO_SOURCES);
    assertTrue(result.get().isEmpty());
  }

  /**
   * Type-use annotations live on the {@code TypeMirror}, not the element — {@code
   * extractAnnotations} on the field's type must find them.
   */
  @Test
  void extractAnnotations_onTypeMirror_findsTypeUseAnnotation() {
    AtomicReference<List<AnnotationDto>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractAnnotations(
                    field(typeElement(processingEnv, "test.Dto"), "typed").asType(),
                    context(processingEnv))),
        DTO_SOURCES);
    assertEquals(1, result.get().size());
    assertEquals("NonNull", result.get().get(0).getAnnotationType().getClassName());
  }

  /**
   * Enum constants, class literals and arrays are the non-trivial branches of {@code
   * formatAnnotationValue}: enums get fully qualified, classes print as {@code .class}, arrays as
   * {@code {...}}.
   */
  @Test
  void extractAnnotations_formatsEnumClassAndArrayMembers() {
    AtomicReference<List<AnnotationDto>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractAnnotations(
                    field(typeElement(processingEnv, "test.Dto"), "rich"), context(processingEnv))),
        DTO_SOURCES);
    assertEquals(1, result.get().size());
    AnnotationDto rich = result.get().get(0);
    assertEquals("test.ItemCondition.BAD", rich.getMembers().get("cond"));
    assertTrue(
        rich.getMembers().get("type").endsWith("String.class"),
        () -> "type member: " + rich.getMembers().get("type"));
    assertEquals("{\"alpha\", \"beta\"}", rich.getMembers().get("tags"));
  }

  @Test
  void extractAnnotations_emptyArrayMember_formatsAsEmptyBraces() {
    AtomicReference<List<AnnotationDto>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractAnnotations(
                    field(typeElement(processingEnv, "test.Dto"), "richEmpty"),
                    context(processingEnv))),
        DTO_SOURCES);
    assertEquals(1, result.get().size());
    assertEquals("{}", result.get().get(0).getMembers().get("tags"));
  }

  @Test
  void extractAnnotationValue_emptyWhenAnnotationHasNoValueMember() {
    AtomicReference<Optional<String>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractAnnotationValue(
                    field(typeElement(processingEnv, "test.Dto"), "required"), Set.of("NonNull"))),
        DTO_SOURCES);
    assertTrue(result.get().isEmpty());
  }

  @Test
  void extractAnnotationValue_emptyWhenNoNameMatches() {
    AtomicReference<Optional<String>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                FieldAnnotationExtractor.extractAnnotationValue(
                    field(typeElement(processingEnv, "test.Dto"), "required"), Set.of("Other"))),
        DTO_SOURCES);
    assertTrue(result.get().isEmpty());
  }
}
