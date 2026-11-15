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

package org.javahelpers.codegen.roaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.method.CodeTemplateDto;
import org.javahelpers.codegen.model.method.MethodCodePlaceholder;
import org.javahelpers.codegen.model.method.MethodCodeStringPlaceholder;
import org.javahelpers.codegen.model.method.MethodCodeTypePlaceholder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.javahelpers.codegen.roaster.exceptions.RoasterMapperException;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link RoasterMapper}'s type/annotation/code-template rendering. */
class RoasterMapperTest {

  @Test
  void mapType_rendersPlainClassName() {
    assertEquals("String", RoasterMapper.mapType(TypeName.of(String.class)));
  }

  @Test
  void mapType_rendersVariableName() {
    assertEquals("T", RoasterMapper.mapType(new TypeNameVariable("T")));
  }

  @Test
  void mapType_rendersPrimitive() {
    assertEquals("int", RoasterMapper.mapType(TypeNamePrimitive.INT));
  }

  @Test
  void mapType_rendersArrayOfInnerType() {
    assertEquals("String[]", RoasterMapper.mapType(new TypeNameArray(TypeName.of(String.class))));
  }

  @Test
  void mapType_rendersGenericWithInnerArguments() {
    TypeNameGeneric generic =
        new TypeNameGeneric(new TypeName("java.util", "List"), List.of(TypeName.of(String.class)));
    assertEquals("List<String>", RoasterMapper.mapType(generic));
  }

  @Test
  void mapType_rendersRawGenericWithoutArguments() {
    TypeNameGeneric generic = new TypeNameGeneric(new TypeName("java.util", "List"), List.of());
    assertEquals("List", RoasterMapper.mapType(generic));
  }

  @Test
  void mapType_prependsTypeUseAnnotations() {
    TypeName annotated = new TypeName("java.lang", "String");
    AnnotationDto nonNull = new AnnotationDto();
    nonNull.setAnnotationType(new TypeName("test.anno", "NonNull"));
    annotated.addAnnotation(nonNull);
    assertEquals("@NonNull String", RoasterMapper.mapType(annotated));
  }

  @Test
  void mapBoxedType_boxesAllPrimitives() {
    assertEquals("Boolean", RoasterMapper.mapBoxedType(TypeNamePrimitive.BOOLEAN));
    assertEquals("Byte", RoasterMapper.mapBoxedType(TypeNamePrimitive.BYTE));
    assertEquals("Character", RoasterMapper.mapBoxedType(TypeNamePrimitive.CHAR));
    assertEquals("Double", RoasterMapper.mapBoxedType(TypeNamePrimitive.DOUBLE));
    assertEquals("Float", RoasterMapper.mapBoxedType(TypeNamePrimitive.FLOAT));
    assertEquals("Integer", RoasterMapper.mapBoxedType(TypeNamePrimitive.INT));
    assertEquals("Long", RoasterMapper.mapBoxedType(TypeNamePrimitive.LONG));
    assertEquals("Short", RoasterMapper.mapBoxedType(TypeNamePrimitive.SHORT));
    assertEquals("Void", RoasterMapper.mapBoxedType(TypeNamePrimitive.VOID));
  }

  @Test
  void mapBoxedType_passesThroughNonPrimitives() {
    assertEquals("String", RoasterMapper.mapBoxedType(TypeName.of(String.class)));
  }

  @Test
  void mapAnnotation_rendersMarkerWithoutMembers() {
    AnnotationDto annotation = new AnnotationDto();
    annotation.setAnnotationType(new TypeName("test.anno", "Marker"));
    assertEquals("@Marker", RoasterMapper.mapAnnotation(annotation));
  }

  @Test
  void mapAnnotation_rendersValueMemberBareAndOthersNamed() {
    AnnotationDto annotation = new AnnotationDto();
    annotation.setAnnotationType(new TypeName("test.anno", "Meta"));
    annotation.addMember("value", "\"x\"");
    annotation.addMember("order", "3");
    assertEquals("@Meta(\"x\", order = 3)", RoasterMapper.mapAnnotation(annotation));
  }

  @Test
  void mapAnnotation_wrapsFailuresInMapperException() {
    AnnotationDto annotation = new AnnotationDto();
    annotation.setAnnotationType(
        new TypeName("test.anno", "Broken") {
          @Override
          public List<AnnotationDto> getAnnotations() {
            throw new IllegalStateException("boom");
          }
        });
    assertThrows(RoasterMapperException.class, () -> RoasterMapper.mapAnnotation(annotation));
  }

  @Test
  void mapInterfaceToTypeName_rendersSimpleAndParameterized() {
    InterfaceName plain = new InterfaceName("java.util.function", "Supplier");
    assertEquals("Supplier", RoasterMapper.mapInterfaceToTypeName(plain));

    InterfaceName generic = new InterfaceName("java.util.function", "Supplier");
    generic.addTypeParameter(TypeName.of(String.class));
    assertEquals("Supplier<String>", RoasterMapper.mapInterfaceToTypeName(generic));
  }

  @Test
  void mapInterfaceToTypeName_prependsTypeUseAnnotations() {
    InterfaceName iface = new InterfaceName("java.io", "Serializable");
    AnnotationDto marker = new AnnotationDto();
    marker.setAnnotationType(new TypeName("test.anno", "Tag"));
    iface.addAnnotation(marker);
    assertEquals("@Tag Serializable", RoasterMapper.mapInterfaceToTypeName(iface));
  }

  @Test
  void mapInterfaceToTypeName_wrapsFailuresInMapperException() {
    InterfaceName iface =
        new InterfaceName("java.io", "Serializable") {
          @Override
          public List<TypeName> getTypeParameters() {
            throw new IllegalStateException("boom");
          }
        };
    iface.addTypeParameter(TypeName.of(String.class));
    // hasTypeParameters() calls getTypeParameters() -> throws
    assertThrows(RoasterMapperException.class, () -> RoasterMapper.mapInterfaceToTypeName(iface));
  }

  @Test
  void resolveCodeTemplate_replacesStringAndTypePlaceholders() {
    CodeTemplateDto code = new CodeTemplateDto();
    code.setCodeFormat("return $name:S, $type:T, $boxed:B, $raw:N;");
    code.addCodeArgument(new MethodCodeStringPlaceholder("name", "val"));
    code.addCodeArgument(new MethodCodeTypePlaceholder("type", TypeNamePrimitive.INT));
    code.addCodeArgument(new MethodCodeTypePlaceholder("boxed", TypeNamePrimitive.INT));
    code.addCodeArgument(new MethodCodeStringPlaceholder("raw", "literal"));

    assertEquals("return \"val\", int, Integer, literal;", RoasterMapper.resolveCodeTemplate(code));
  }

  @Test
  void resolveCodeTemplate_rewritesTrackedValueFactoryNames() {
    CodeTemplateDto code = new CodeTemplateDto();
    code.setCodeFormat(
        "TrackedValue.initialValue(x); TrackedValue.changedValue(y); TrackedValue.unsetValue();");
    assertEquals(
        "initialValue(x); changedValue(y); unsetValue();", RoasterMapper.resolveCodeTemplate(code));
  }

  @Test
  void resolveCodeTemplate_rejectsUnknownPlaceholderKinds() {
    CodeTemplateDto code = new CodeTemplateDto();
    code.setCodeFormat("return $x:X;");
    code.addCodeArgument(new MethodCodePlaceholder<String>("x", "v") {});
    assertThrows(RoasterMapperException.class, () -> RoasterMapper.resolveCodeTemplate(code));
  }

  @Test
  void quote_escapesSpecialCharacters() {
    assertEquals("\"a\\\"b\\\\c\\n\\t\"", RoasterMapper.quote("a\"b\\c\n\t"));
  }

  @Test
  void packageNameOf_stripsClassName() {
    assertEquals("java.util", RoasterMapper.packageNameOf("java.util.List"));
    assertEquals("", RoasterMapper.packageNameOf("List"));
  }
}
