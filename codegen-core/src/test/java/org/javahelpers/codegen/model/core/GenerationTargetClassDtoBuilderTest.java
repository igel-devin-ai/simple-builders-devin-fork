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

package org.javahelpers.codegen.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.imports.RegularImport;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.junit.jupiter.api.Test;

/** Tests for the generated {@link GenerationTargetClassDtoBuilder}. */
class GenerationTargetClassDtoBuilderTest {

  @Test
  void buildsEquivalentDtoAsSetterSequence() {
    // Same DTO as assembled field-by-field in RoasterCodeGeneratorTest
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "AnnotatedBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .classJavadoc(new JavadocDto("Builder for {@link Person}."))
            .superType(new TypeName("test", "AbstractBuilder"))
            .build();

    assertEquals(new TypeName("test", "AnnotatedBuilder"), dto.getTypeName());
    assertEquals(AccessModifier.PUBLIC, dto.getClassAccessModifier());
    assertEquals(FormattingMode.NONE, dto.getFormattingMode());
    assertEquals("Builder for {@link Person}.", dto.getClassJavadoc().getDescription());
    assertEquals(new TypeName("test", "AbstractBuilder"), dto.getSuperType());
  }

  @Test
  void buildsNestedDtoViaBuilderConsumer() {
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "EmptyBuilder"))
            .classJavadoc(javadoc -> javadoc.format("Builder for {@link %s}.", "Person"))
            .build();

    assertEquals("Builder for {@link Person}.", dto.getClassJavadoc().getDescription());
  }

  @Test
  void unsetPropertiesRemainNull() {
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "EmptyBuilder"))
            .build();

    assertNull(dto.getClassAccessModifier());
    assertNull(dto.getClassJavadoc());
    assertNull(dto.getSuperType());
    assertNull(dto.getFormattingMode());
  }

  @Test
  void builderInitializedFromInstanceAppliesChanges() {
    GenerationTargetClassDto original =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "Builder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .build();

    GenerationTargetClassDto modified =
        new GenerationTargetClassDtoBuilder(original)
            .classAccessModifier(AccessModifier.PRIVATE)
            .build();

    assertEquals(original.getTypeName(), modified.getTypeName());
    assertEquals(AccessModifier.PRIVATE, modified.getClassAccessModifier());
    assertEquals(AccessModifier.PUBLIC, original.getClassAccessModifier());
  }

  @Test
  void buildsWholeDtoInOneFluentStream() {
    // One continuous stream: every nested DTO is configured through the
    // Consumer-based collection helpers, no intermediate variables.
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName("example", "Target")
            .classFields(
                fields ->
                    fields.add(field -> field.fieldName("value").fieldType("java.lang", "String")))
            .constructors(
                constructors ->
                    constructors.add(
                        constructor ->
                            constructor.parameters(
                                parameters ->
                                    parameters.add(
                                        parameter ->
                                            parameter
                                                .parameterName("arg")
                                                .parameterTypeName("java.lang", "String")))))
            .methods(
                methods ->
                    methods.add(
                        method ->
                            method
                                .methodName("apply")
                                .returnType("java.lang", "String")
                                .parameters(
                                    parameters ->
                                        parameters.add(
                                            parameter ->
                                                parameter
                                                    .parameterName("v")
                                                    .parameterTypeName("java.lang", "Integer")))
                                .genericParameters(
                                    generics ->
                                        generics.add(
                                            generic ->
                                                generic
                                                    .name("T")
                                                    .upperBounds(TypeName.of(Number.class))))
                                .annotations(
                                    annotations ->
                                        annotations.add(
                                            annotation ->
                                                annotation.annotationType(
                                                    "java.lang", "Deprecated")))))
            .generics(generics -> generics.add(generic -> generic.name("E")))
            .interfaces(interfaces -> interfaces.add(new InterfaceName("java.lang", "Runnable")))
            .nestedTypes(
                nestedTypes ->
                    nestedTypes.add(
                        nested ->
                            nested
                                .typeName("Factory")
                                .methods(
                                    methods -> methods.add(method -> method.methodName("create")))))
            .classAnnotations(
                annotations ->
                    annotations.add(
                        annotation ->
                            annotation
                                .annotationType("java.lang", "Deprecated")
                                .members(Map.of("since", "\"1\""))))
            .imports(imports -> imports.add(new RegularImport(TypeName.of(List.class))))
            .build();

    assertEquals(new TypeName("example", "Target"), dto.getTypeName());
    assertEquals("value", dto.getClassFields().get(0).getFieldName());
    assertEquals(TypeName.of(String.class), dto.getClassFields().get(0).getFieldType());
    assertEquals("arg", dto.getConstructors().get(0).getParameters().get(0).getParameterName());
    assertEquals(
        TypeName.of(String.class),
        dto.getConstructors().get(0).getParameters().get(0).getParameterType());
    assertEquals("apply", dto.getMethods().get(0).getMethodName());
    assertEquals(TypeName.of(String.class), dto.getMethods().get(0).getReturnType());
    assertEquals("v", dto.getMethods().get(0).getParameters().get(0).getParameterName());
    assertEquals("T", dto.getMethods().get(0).getGenericParameters().get(0).getName());
    assertEquals("E", dto.getGenerics().get(0).getName());
    assertEquals(Set.of(new InterfaceName("java.lang", "Runnable")), dto.getInterfaces());
    assertEquals("Factory", dto.getNestedTypes().get(0).getTypeName());
    assertEquals("create", dto.getNestedTypes().get(0).getMethods().get(0).getMethodName());
    assertEquals(
        Map.of("since", "\"1\""), dto.getClassAnnotations().iterator().next().getMembers());
    assertEquals(Set.of(new RegularImport(TypeName.of(List.class))), dto.getImports());
  }

  @Test
  void methodDtoBuilderCoversIsStaticFlag() {
    // The isStatic getter supplies a legal property name for the setStatic setter.
    MethodDto method = MethodDtoBuilder.create().methodName("m").isStatic(true).build();

    assertEquals("m", method.getMethodName());
    assertTrue(method.isStatic());
  }
}
