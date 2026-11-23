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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameSet;
import org.javahelpers.simple.builders.processor.generators.util.JavadocExampleValues;
import org.javahelpers.simple.builders.processor.model.type.ResolvedBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavadocExampleValuesTest {

  private static TypeName stringType() {
    return TypeName.of(String.class);
  }

  private static TypeName listOf(TypeName elementType) {
    return new TypeNameList(TypeName.of(List.class), List.of(elementType), elementType);
  }

  private static TypeName setOf(TypeName elementType) {
    return new TypeNameSet(TypeName.of(java.util.Set.class), List.of(elementType), elementType);
  }

  private static TypeName mapOf(TypeName keyType, TypeName valueType) {
    return new TypeNameMap(TypeName.of(Map.class), List.of(keyType, valueType), keyType, valueType);
  }

  private static TypeName typeWithEmptyConstructor() {
    TypeName type = new TypeName("test", "AddressDto");
    type.setHasEmptyConstructor(true);
    return type;
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("supportedTypeExamples")
  void shouldReturnExampleForSupportedType(TypeName type, String fieldName, String expected) {
    assertEquals(
        Optional.of(expected),
        JavadocExampleValues.getExampleValue(type, fieldName, t -> Optional.empty()));
  }

  private static Stream<Arguments> supportedTypeExamples() {
    return Stream.of(
        // Primitives
        Arguments.of(TypeNamePrimitive.INT, "count", "42"),
        Arguments.of(TypeNamePrimitive.LONG, "count", "42L"),
        Arguments.of(TypeNamePrimitive.DOUBLE, "ratio", "3.14"),
        Arguments.of(TypeNamePrimitive.FLOAT, "ratio", "3.14f"),
        Arguments.of(TypeNamePrimitive.BOOLEAN, "active", "true"),
        Arguments.of(TypeNamePrimitive.CHAR, "letter", "'x'"),
        // Wrapper types
        Arguments.of(TypeName.of(Integer.class), "count", "42"),
        Arguments.of(TypeName.of(Long.class), "count", "42L"),
        Arguments.of(TypeName.of(Double.class), "ratio", "3.14"),
        Arguments.of(TypeName.of(Float.class), "ratio", "3.14f"),
        Arguments.of(TypeName.of(Boolean.class), "active", "true"),
        Arguments.of(TypeName.of(Character.class), "letter", "'x'"),
        // Common JDK types
        Arguments.of(TypeName.of(BigInteger.class), "amount", "BigInteger.valueOf(42)"),
        Arguments.of(TypeName.of(BigDecimal.class), "amount", "BigDecimal.valueOf(3.14)"),
        Arguments.of(TypeName.of(UUID.class), "id", "UUID.randomUUID()"),
        Arguments.of(TypeName.of(LocalDate.class), "day", "LocalDate.now()"),
        Arguments.of(TypeName.of(LocalTime.class), "time", "LocalTime.now()"),
        Arguments.of(TypeName.of(LocalDateTime.class), "stamp", "LocalDateTime.now()"),
        // String uses the field name to derive the example
        Arguments.of(stringType(), "category", "\"example category\""),
        // Collection and composite types
        Arguments.of(listOf(stringType()), "tags", "List.of(\"example tags\")"),
        Arguments.of(setOf(TypeName.of(Integer.class)), "ids", "Set.of(42)"),
        Arguments.of(
            mapOf(stringType(), TypeName.of(Integer.class)),
            "values",
            "Map.of(\"example values\", 42)"),
        Arguments.of(new TypeNameArray(stringType()), "tags", "new String[] {\"example tags\"}"),
        Arguments.of(
            new TypeNameGeneric(Optional.class, String.class),
            "subtitle",
            "Optional.of(\"example subtitle\")"),
        // Types with an empty constructor or a builder
        Arguments.of(typeWithEmptyConstructor(), "address", "new AddressDto()"));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("unsupportedTypeExamples")
  void shouldReturnEmptyForUnsupportedType(TypeName type, String description) {
    assertEquals(
        Optional.empty(),
        JavadocExampleValues.getExampleValue(type, "value", t -> Optional.empty()));
  }

  private static Stream<Arguments> unsupportedTypeExamples() {
    return Stream.of(
        // Primitives without a mapped example
        Arguments.of(TypeNamePrimitive.BYTE, "byte primitive"),
        Arguments.of(TypeNamePrimitive.SHORT, "short primitive"),
        // Raw collections and maps are not parameterized
        Arguments.of(new TypeNameList(TypeName.of(List.class), List.of(), null), "raw List"),
        Arguments.of(new TypeNameMap(TypeName.of(Map.class), List.of(), null, null), "raw Map"),
        // Unresolvable element types yield no example
        Arguments.of(
            listOf(new TypeName("test", "Unsupported")), "List of unsupported element type"),
        Arguments.of(
            mapOf(stringType(), new TypeName("test", "Unsupported")),
            "Map with unsupported value type"),
        Arguments.of(
            new TypeNameGeneric(Optional.class, java.io.File.class),
            "Optional of unsupported type"),
        Arguments.of(new TypeName("test", "Unsupported"), "unsupported type"));
  }

  @Test
  void shouldUseBuilderBackedExampleForResolvableType() {
    TypeName sponsorType = new TypeName("test", "SponsorDto");
    assertEquals(
        Optional.of("SponsorDtoBuilder.create().build()"),
        JavadocExampleValues.getExampleValue(
            sponsorType,
            "sponsor",
            t -> Optional.of(new ResolvedBuilder(new TypeName("test", "SponsorDtoBuilder")))));
  }

  @Test
  void shouldUseBuilderBackedExampleForCollectionElement() {
    TypeName sponsorType = new TypeName("test", "SponsorDto");
    assertEquals(
        Optional.of("List.of(SponsorDtoBuilder.create().build())"),
        JavadocExampleValues.getExampleValue(
            listOf(sponsorType),
            "sponsors",
            t -> Optional.of(new ResolvedBuilder(new TypeName("test", "SponsorDtoBuilder")))));
  }

  @Test
  void shouldDeriveStringExampleFromFieldName() {
    assertEquals(
        Optional.of("\"example category\""),
        JavadocExampleValues.getExampleValue(stringType(), "category", t -> Optional.empty()));
  }

  @Test
  void shouldUseDefaultStringExampleForBlankNames() {
    for (String fieldName : new String[] {null, "", " \t\n"}) {
      assertEquals(
          Optional.of("\"example value\""),
          JavadocExampleValues.getExampleValue(stringType(), fieldName, t -> Optional.empty()));
    }
  }

  @Test
  void shouldOmitExampleForUnsupportedType() {
    assertEquals(
        Optional.empty(),
        JavadocExampleValues.getExampleValue(
            new TypeName("test", "Unsupported"), "category", t -> Optional.empty()));
  }
}
