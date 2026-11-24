package org.javahelpers.simple.builders.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto;
import org.javahelpers.simple.builders.processor.model.core.BuilderFieldDtoBuilder;
import org.javahelpers.simple.builders.processor.model.core.GetterInfoDto;
import org.javahelpers.simple.builders.processor.model.core.GetterInfoDtoBuilder;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDto;
import org.javahelpers.simple.builders.processor.model.method.BuilderMethodDtoBuilder;
import org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDto;
import org.javahelpers.simple.builders.processor.model.type.BuilderNestedTypeDtoBuilder;
import org.junit.jupiter.api.Test;

/** Covers the committed builders of the processor-internal DTOs. */
class ProcessorDtoBuildersTest {

  @Test
  void builderFieldDtoBuilderCoversAllProperties() {
    BuilderFieldDto dto =
        BuilderFieldDtoBuilder.create()
            .originalFieldName("persons")
            .fieldNameInBuilder("persons")
            .setterName("setPersons")
            .fieldType(String.class)
            .nonNullable(true)
            .fieldGenerics(generics -> generics.add(g -> g.name("T")))
            .parameterAnnotations(
                annotations -> annotations.add(a -> a.annotationType("java.lang", "Deprecated")))
            .defaultValue("\"none\"")
            .build();

    assertEquals("persons", dto.getOriginalFieldName());
    assertEquals("persons", dto.getFieldNameInBuilder());
    assertEquals("setPersons", dto.getSetterName().orElseThrow());
    assertEquals(TypeName.of(String.class), dto.getFieldType());
    assertTrue(dto.isNonNullable());
    assertEquals("T", dto.getFieldGenerics().get(0).getName());
    assertEquals(
        new TypeName("java.lang", "Deprecated"),
        dto.getParameterAnnotations().get(0).getAnnotationType());
    assertEquals("\"none\"", dto.getDefaultValue().orElseThrow());

    BuilderFieldDto copy = dto.with(b -> b.originalFieldName("renamed"));
    assertEquals("renamed", copy.getOriginalFieldName());
    assertEquals("persons", dto.getOriginalFieldName());
  }

  @Test
  void getterInfoDtoBuilderBuildsRecord() {
    GetterInfoDto dto =
        GetterInfoDtoBuilder.create().getterName("getPersons").deprecated(false).build();

    assertEquals("getPersons", dto.getGetterName());
    assertFalse(dto.deprecated());

    GetterInfoDto copy = dto.with(b -> b.deprecated(true));
    assertTrue(copy.deprecated());
    assertFalse(dto.deprecated());
  }

  @Test
  void builderMethodDtoBuilderCoversAllProperties() {
    BuilderMethodDto dto =
        BuilderMethodDtoBuilder.create()
            .methodName("persons")
            .returnType(String.class)
            .code("return $builder:N.$fieldName:N;")
            .priority(42)
            .ordering(7)
            .isStatic(true)
            .modifier(AccessModifier.PRIVATE)
            .sourceFieldName("persons")
            .build();
    dto.getParameters().add(new org.javahelpers.codegen.model.method.MethodParameterDto());

    assertEquals("persons", dto.getMethodName());
    assertEquals(TypeName.of(String.class), dto.getReturnType());
    assertTrue(dto.hasCode());
    assertEquals(42, dto.getPriority());
    assertEquals(7, dto.getOrdering());
    assertTrue(dto.isStatic());
    assertEquals(AccessModifier.PRIVATE, dto.getModifier().orElseThrow());
    assertEquals("persons", dto.getSourceFieldName());
    assertEquals(1, dto.getParameters().size());

    BuilderMethodDto copy = dto.with(b -> b.methodName("renamed"));
    assertEquals("renamed", copy.getMethodName());
    assertEquals("persons", dto.getMethodName());
  }

  @Test
  void builderNestedTypeDtoBuilderCoversAllProperties() {
    BuilderNestedTypeDto dto =
        BuilderNestedTypeDtoBuilder.create()
            .typeName("Factory")
            .kind(NestedTypeDto.NestedTypeKind.CLASS)
            .visibility(AccessModifier.PROTECTED)
            .build();
    dto.getMethods().add(BuilderMethodDtoBuilder.create().methodName("create").build());

    assertEquals("Factory", dto.getTypeName());
    assertEquals(NestedTypeDto.NestedTypeKind.CLASS, dto.getKind());
    assertEquals(AccessModifier.PROTECTED, dto.getVisibility());
    assertEquals("create", dto.getMethods().get(0).getMethodName());

    BuilderNestedTypeDto copy = dto.with(b -> b.typeName("Renamed"));
    assertEquals("Renamed", copy.getTypeName());
    assertEquals("Factory", dto.getTypeName());
  }
}
