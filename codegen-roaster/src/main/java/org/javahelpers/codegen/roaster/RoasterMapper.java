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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.method.CodeTemplateDto;
import org.javahelpers.codegen.model.method.MethodCodePlaceholder;
import org.javahelpers.codegen.model.method.MethodCodeStringPlaceholder;
import org.javahelpers.codegen.model.method.MethodCodeTypePlaceholder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameNested;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.javahelpers.codegen.roaster.exceptions.RoasterMapperException;

/** Helper functions to create Roaster-compatible source code strings from DTOs. */
public final class RoasterMapper {

  private RoasterMapper() {}

  /**
   * Maps a type model to Java source code, qualifying types whose simple name is ambiguous within
   * the generated class.
   *
   * @param typeName type to map
   * @param collidingSimpleNames simple names used by more than one distinct type in the generated
   *     class; affected types are emitted fully qualified (e.g. {@code java.sql.Date} next to
   *     {@code java.util.Date})
   * @return Java source representation of the type
   */
  public static String mapType(TypeName typeName, Set<String> collidingSimpleNames) {
    String mappedType;
    if (typeName instanceof TypeNameVariable) {
      mappedType = typeName.getClassName();
    } else if (typeName instanceof TypeNamePrimitive primitive) {
      mappedType = primitive.getFullQualifiedName();
    } else if (typeName instanceof TypeNameArray arrayType) {
      mappedType = mapType(arrayType.getTypeOfArray(), collidingSimpleNames) + "[]";
    } else if (typeName instanceof TypeNameGeneric genericType) {
      mappedType = mapGenericType(genericType, collidingSimpleNames);
    } else if (typeName instanceof TypeNameNested) {
      // Roaster splits a bare "Outer.Inner" into package "Outer" + class "Inner"; passing the
      // full qualified name makes it emit "import pkg.Outer.Inner" and use "Inner" in code.
      mappedType = typeName.getFullQualifiedName();
    } else {
      mappedType =
          collidingSimpleNames.contains(typeName.getClassName())
              ? typeName.getFullQualifiedName()
              : typeName.getClassName();
    }

    return prependTypeUseAnnotations(mappedType, typeName.getAnnotations(), collidingSimpleNames);
  }

  /**
   * Maps a type to a boxed Java source representation, qualifying ambiguous simple names.
   *
   * @param typeName type to map
   * @param collidingSimpleNames simple names used by more than one distinct type in the generated
   *     class
   * @return boxed Java source representation of the type
   */
  public static String mapBoxedType(TypeName typeName, Set<String> collidingSimpleNames) {
    if (typeName instanceof TypeNamePrimitive primitive) {
      String boxedType =
          switch (primitive.getType()) {
            case BOOLEAN -> Boolean.class.getSimpleName();
            case BYTE -> Byte.class.getSimpleName();
            case CHAR -> Character.class.getSimpleName();
            case DOUBLE -> Double.class.getSimpleName();
            case FLOAT -> Float.class.getSimpleName();
            case INT -> Integer.class.getSimpleName();
            case LONG -> Long.class.getSimpleName();
            case SHORT -> Short.class.getSimpleName();
            case VOID -> Void.class.getSimpleName();
          };
      return prependTypeUseAnnotations(boxedType, typeName.getAnnotations(), collidingSimpleNames);
    }
    return mapType(typeName, collidingSimpleNames);
  }

  private static String mapGenericType(
      TypeNameGeneric genericType, Set<String> collidingSimpleNames) {
    // Nested generic types keep the fully qualified name: Roaster parses "pkg.Outer.Pair<T>" by
    // importing "pkg.Outer.Pair" and using "Pair<T>" in code. The raw type's FQN is used because
    // TypeNameGeneric.getFullQualifiedName() already includes the type arguments.
    String baseName =
        genericType.isNestedType()
            ? genericType.getRawType().getFullQualifiedName()
            : collidingSimpleNames.contains(genericType.getClassName())
                ? genericType.getRawType().getFullQualifiedName()
                : genericType.getClassName();
    if (genericType.getInnerTypeArguments().isEmpty()) {
      return baseName;
    }
    String innerTypes =
        genericType.getInnerTypeArguments().stream()
            .map(inner -> mapBoxedType(inner, collidingSimpleNames))
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    return baseName + "<" + innerTypes + ">";
  }

  /**
   * Maps an annotation to source code, qualifying ambiguous simple names.
   *
   * @param annotationDto annotation DTO
   * @param collidingSimpleNames simple names used by more than one distinct type in the generated
   *     class
   * @return Java source representation of the annotation
   */
  public static String mapAnnotation(
      AnnotationDto annotationDto, Set<String> collidingSimpleNames) {
    try {
      String annotationType = mapType(annotationDto.getAnnotationType(), collidingSimpleNames);
      if (annotationDto.getMembers().isEmpty()) {
        return "@" + annotationType;
      }
      String members =
          annotationDto.getMembers().entrySet().stream()
              .map(RoasterMapper::mapAnnotationMember)
              .reduce((a, b) -> a + ", " + b)
              .orElse("");
      return "@" + annotationType + "(" + members + ")";
    } catch (Exception e) {
      throw new RoasterMapperException(
          e,
          "Failed to map annotation %s: %s",
          annotationDto.getAnnotationType().getClassName(),
          e.getMessage());
    }
  }

  private static String mapAnnotationMember(Map.Entry<String, String> member) {
    if ("value".equals(member.getKey())) {
      return member.getValue();
    }
    return member.getKey() + " = " + member.getValue();
  }

  /**
   * Maps an interface name to Java source code, qualifying ambiguous simple names.
   *
   * @param interfaceName interface model
   * @param collidingSimpleNames simple names used by more than one distinct type in the generated
   *     class
   * @return source representation of the interface type
   */
  public static String mapInterfaceToTypeName(
      InterfaceName interfaceName, Set<String> collidingSimpleNames) {
    try {
      String qualifiedName =
          collidingSimpleNames.contains(interfaceName.getSimpleName())
              ? interfaceName.getPackageName() + "." + interfaceName.getSimpleName()
              : interfaceName.getSimpleName();
      if (interfaceName.hasTypeParameters()) {
        String typeParameters =
            interfaceName.getTypeParameters().stream()
                .map(t -> mapType(t, collidingSimpleNames))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        qualifiedName = qualifiedName + "<" + typeParameters + ">";
      }
      return prependTypeUseAnnotations(
          qualifiedName, interfaceName.getAnnotations(), collidingSimpleNames);
    } catch (Exception e) {
      throw new RoasterMapperException(
          e, "Failed to map interface %s: %s", interfaceName.toString(), e.getMessage());
    }
  }

  /**
   * Resolves the JavaPoet-style named template used in CodeTemplateDto to plain Java source code,
   * qualifying ambiguous simple names.
   *
   * @param codeDto code template DTO
   * @param collidingSimpleNames simple names used by more than one distinct type in the generated
   *     class
   * @return resolved Java source code
   */
  public static String resolveCodeTemplate(
      CodeTemplateDto codeDto, Set<String> collidingSimpleNames) {
    String code = codeDto.getCodeFormat();
    for (MethodCodePlaceholder<?> placeHolderValue : codeDto.getCodeArguments()) {
      String label = placeHolderValue.getLabel();
      if (placeHolderValue instanceof MethodCodeStringPlaceholder stringPlaceholder) {
        code = code.replace("$" + label + ":N", stringPlaceholder.getValue());
        code = code.replace("$" + label + ":L", stringPlaceholder.getValue());
        code = code.replace("$" + label + ":S", quote(stringPlaceholder.getValue()));
      } else if (placeHolderValue instanceof MethodCodeTypePlaceholder typePlaceholder) {
        code =
            code.replace(
                "$" + label + ":T", mapType(typePlaceholder.getValue(), collidingSimpleNames));
        code =
            code.replace(
                "$" + label + ":B", mapBoxedType(typePlaceholder.getValue(), collidingSimpleNames));
      } else {
        throw new RoasterMapperException(
            "Unsupported placeholder type: %s", placeHolderValue.getClass().getName());
      }
    }
    code = code.replace("TrackedValue.initialValue", "initialValue");
    code = code.replace("TrackedValue.changedValue", "changedValue");
    code = code.replace("TrackedValue.unsetValue", "unsetValue");
    return code;
  }

  /**
   * Converts plain text to a Java string literal.
   *
   * @param value text value
   * @return quoted and escaped Java string literal
   */
  public static String quote(String value) {
    String escaped =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    return "\"" + escaped + "\"";
  }

  /**
   * Extracts package name from fully qualified name.
   *
   * @param fqn fully qualified name
   * @return package name or empty string if no package
   */
  public static String packageNameOf(String fqn) {
    int idx = fqn.lastIndexOf('.');
    return idx < 0 ? "" : fqn.substring(0, idx);
  }

  private static String prependTypeUseAnnotations(
      String baseType, List<AnnotationDto> annotations, Set<String> collidingSimpleNames) {
    if (CollectionUtils.isEmpty(annotations)) {
      return baseType;
    }
    String prefix =
        annotations.stream()
            .map(a -> mapAnnotation(a, collidingSimpleNames))
            .reduce((a, b) -> a + " " + b)
            .orElse("");
    return prefix + " " + baseType;
  }
}
