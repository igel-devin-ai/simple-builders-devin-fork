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

import static org.javahelpers.codegen.testing.ProcessorAsserts.assertNormalizedEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.util.List;
import java.util.Map;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocTagDto;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.javahelpers.codegen.testing.TestingGenerationEnvironment;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RoasterCodeGenerator} driving {@link GenerationTargetClassDto}s directly — no
 * simple-builder types involved, the {@code GenerationEnvironment} comes from shared test support
 * ({@link TestingGenerationEnvironment}) and the emitted source is asserted as a whole text block.
 */
class RoasterCodeGeneratorTest {

  @Test
  void preservesLeadingFieldTypeAnnotation() throws Exception {
    GenerationTargetClassDto classDef =
        annotatedClassDefinition()
            .with(
                b ->
                    b.classFields(
                        fields ->
                            fields.add(
                                f ->
                                    f.fieldName("value")
                                        .fieldType(stringTypeNullableAnnotated())
                                        .visibility(AccessModifier.PRIVATE))));

    assertAnnotatedSource(
        """
        package test;
        import test.anno.Nullable;
        public class AnnotatedTypes {
          @Nullable
          private String value;
        }
        """,
        classDef);
  }

  @Test
  void preservesLeadingReturnTypeAnnotation() throws Exception {
    GenerationTargetClassDto classDef =
        annotatedClassDefinition()
            .with(
                b ->
                    b.methods(
                        methods ->
                            methods.add(
                                m ->
                                    m.methodName("value")
                                        .returnType(stringTypeNullableAnnotated())
                                        .modifier(AccessModifier.PUBLIC)
                                        .code("return null;")
                                        .annotations(
                                            annotations ->
                                                annotations.add(
                                                    a -> a.annotationType(Deprecated.class))))));

    assertAnnotatedSource(
        """
        package test;
        import test.anno.Nullable;
        public class AnnotatedTypes {
          @Nullable
          @Deprecated
          public String value() {
            return null;
          }
        }
        """,
        classDef);
  }

  @Test
  void preservesAnnotatedGenericReturnType() throws Exception {
    TypeName listOfStringsAnnotated =
        new TypeNameGeneric(TypeName.of(List.class), stringTypeNullableAnnotated());
    TypeName listOfListsAnnotated =
        new TypeNameGeneric(TypeName.of(List.class), listOfStringsAnnotated);
    GenerationTargetClassDto classDef =
        annotatedClassDefinition()
            .with(
                b ->
                    b.methods(
                            methods ->
                                methods.add(
                                    m ->
                                        m.methodName("values")
                                            .returnType(listOfStringsAnnotated)
                                            .modifier(AccessModifier.PUBLIC)
                                            .code("return null;")))
                        .nestedTypes(
                            nestedTypes ->
                                nestedTypes.add(
                                    n ->
                                        n.typeName("Nested")
                                            .kind(NestedTypeDto.NestedTypeKind.INTERFACE)
                                            .visibility(AccessModifier.PUBLIC)
                                            .methods(
                                                methods ->
                                                    methods.add(
                                                        m ->
                                                            m.methodName("values")
                                                                .returnType(listOfListsAnnotated)
                                                                .code("return null;"))))));

    assertAnnotatedSource(
        """
        package test;
        import java.util.List;
        import test.anno.Nullable;
        public class AnnotatedTypes {
          public List<@Nullable String> values() {
            return null;
          }
          public interface Nested {
            default List<List<@Nullable String>> values() {
              return null;
            }
          }
        }
        """,
        classDef);
  }

  @Test
  void generatesDelegatingBuilderFromDto() throws Exception {
    assertGeneratedSource(
        """
        package test;
        public class PersonBuilder {

          private Person target = new Person();

          public PersonBuilder name(String name) {
            target.setName(name);
            return this;
          }

          public Person build() {
            return target;
          }
        }""",
        personBuilderDefinition());
  }

  @Test
  void writesFileUnderQualifiedName() throws Exception {
    GenerationTargetClassDto empty =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("com.example", "EmptyBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .build();

    assertEquals("mem:///com/example/EmptyBuilder.java", generate(empty).writtenUri().toString());
  }

  @Test
  void qualifiesCollidingDateTypeNames() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "DateBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .classFields(
                fields ->
                    fields
                        .add(f -> f.fieldName("utilDate").fieldType("java.util", "Date"))
                        .add(f -> f.fieldName("sqlDate").fieldType("java.sql", "Date")))
            .build();

    assertGeneratedSource(
        """
        package test;
        import java.sql.Date;
        public class DateBuilder {
          private java.util.Date utilDate;
          private Date sqlDate;
        }""",
        classDef);
  }

  @Test
  void qualifiesCollidingGenericListTypeNames() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "ListBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .classFields(
                fields ->
                    fields
                        .add(
                            f ->
                                f.fieldName("utilList")
                                    .fieldType(
                                        new TypeNameGeneric(java.util.List.class, String.class)))
                        .add(f -> f.fieldName("awtList").fieldType("java.awt", "List")))
            .build();

    assertGeneratedSource(
        """
        package test;
        import java.awt.List;
        public class ListBuilder {
          private java.util.List<String> utilList;
          private List awtList;
        }""",
        classDef);
  }

  @Test
  void qualifiesCollidingAnnotationAndInterfaceSimpleNames() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "AmbiguousBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            // Two distinct NonNull annotations on different declaration sites: the one that wins
            // the import renders simple, the other stays qualified.
            .classAnnotations(
                annotations -> annotations.add(a -> a.annotationType("org.a", "NonNull")))
            .methods(
                methods ->
                    methods.add(
                        m ->
                            m.methodName("x")
                                .returnType(String.class)
                                .modifier(AccessModifier.PUBLIC)
                                .code("return null;")
                                .annotations(
                                    annotations ->
                                        annotations.add(a -> a.annotationType("org.b", "NonNull")))
                                .parameters(
                                    parameters ->
                                        parameters.add(
                                            p ->
                                                p.parameterName("in")
                                                    .parameterTypeName(String.class)
                                                    .annotations(
                                                        annotations ->
                                                            annotations.add(
                                                                a ->
                                                                    a.annotationType(
                                                                        "org.a", "NonNull")))))))
            // Two distinct interfaces sharing a simple name.
            .interfaces(
                interfaces ->
                    interfaces
                        .add(new InterfaceName("org.a", "Cloneable"))
                        .add(new InterfaceName("org.b", "Cloneable")))
            .build();

    assertGeneratedSource(
        """
        package test;
        import org.a.Cloneable;
        import org.a.NonNull;
        @NonNull
        public class AmbiguousBuilder implements Cloneable, org.b.Cloneable {
          @org.b.NonNull
          public String x(@NonNull String in) {
            return null;
          }
        }""",
        classDef);
  }

  @Test
  void qualifiesCollidingTypeUseAnnotationSimpleNames() throws Exception {
    // A field type carrying a type-use annotation whose simple name collides with a method
    // annotation type: both usages must render qualified.
    TypeName fieldType = TypeName.of(String.class);
    fieldType.addAnnotation(
        AnnotationDtoBuilder.create().annotationType("org.b", "Nullable").build());
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "AmbiguousBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .methods(
                methods ->
                    methods.add(
                        m ->
                            m.methodName("x")
                                .returnType(String.class)
                                .modifier(AccessModifier.PUBLIC)
                                .code("return null;")
                                .annotations(
                                    annotations ->
                                        annotations.add(
                                            a -> a.annotationType("org.a", "Nullable")))))
            .classFields(fields -> fields.add(f -> f.fieldName("value").fieldType(fieldType)))
            .build();

    assertGeneratedSource(
        """
        package test;
        import org.a.Nullable;
        public class AmbiguousBuilder {
          @org.b.Nullable
          private String value;
          @Nullable
          public String x() {
            return null;
          }
        }""",
        classDef);
  }

  @Test
  void generatesClassMetadataJavadocAndAnnotations() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "AnnotatedBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .classJavadoc(
                JavadocDtoBuilder.create().format("Builder for {@link Person}.").args().build())
            .superType(new TypeName("test", "AbstractBuilder"))
            .classAnnotations(
                annotations ->
                    annotations.add(
                        a ->
                            a.annotationType("javax.annotation", "Generated")
                                .members(Map.of("value", "\"simple-builders\""))))
            .interfaces(interfaces -> interfaces.add(new InterfaceName("java.io", "Serializable")))
            .build();

    assertGeneratedSource(
        """
        package test;
        import java.io.Serializable;
        import javax.annotation.Generated;
        /**
         * Builder for {@link Person}.
         */
        @Generated("simple-builders")
        public class AnnotatedBuilder extends AbstractBuilder implements Serializable {
        }""",
        classDef);
  }

  @Test
  void generatesGenericClassAndMethods() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "GenericBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .generics(generics -> generics.add(g -> g.name("T").add2UpperBounds(Number.class)))
            .methods(
                methods ->
                    methods.add(
                        m ->
                            m.methodName("of")
                                .returnType(new TypeNameVariable("T"))
                                .modifier(AccessModifier.PUBLIC)
                                .isStatic(true)
                                .code("return null;")
                                .genericParameters(
                                    generics ->
                                        generics.add(
                                            g -> g.name("R").add2UpperBounds(Number.class)))
                                .parameters(
                                    parameters ->
                                        parameters.add(
                                            p ->
                                                p.parameterName("input")
                                                    .parameterTypeName(
                                                        new TypeNameVariable("R"))))))
            .build();

    assertGeneratedSource(
        """
        package test;
        public class GenericBuilder<T extends Number> {
          public static <R extends Number> T of(R input) {
            return null;
          }
        }""",
        classDef);
  }

  @Test
  void generatesVoidVarargsAndPackagePrivateMembers() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "MiscBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .methods(
                methods ->
                    methods.add(
                        m ->
                            m.methodName("close")
                                .modifier(AccessModifier.PACKAGE_PRIVATE)
                                .parameters(
                                    parameters ->
                                        parameters.add(
                                            p ->
                                                p.parameterName("items")
                                                    .parameterTypeNameArray(String.class)))
                                .annotations(
                                    annotations ->
                                        annotations.add(
                                            a ->
                                                a.annotationType(SuppressWarnings.class)
                                                    .members(Map.of("value", "\"unchecked\""))))))
            .build();

    assertGeneratedSource(
        """
        package test;
        public class MiscBuilder {
          @SuppressWarnings("unchecked")
          void close(String... items) {
          }
        }""",
        classDef);
  }

  @Test
  void generatesJavadocCodeBlockAndValuelessTag() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "DocBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .methods(
                methods ->
                    methods.add(
                        m ->
                            m.methodName("apply")
                                .returnType(TypeNamePrimitive.VOID)
                                .modifier(AccessModifier.PUBLIC)
                                .javadoc(
                                    javadoc ->
                                        javadoc
                                            .format("Applies the values.")
                                            .args()
                                            .add2Tags(JavadocTagDto.withoutValue("since"))
                                            .exampleUsageCodeBlock(
                                                JavadocCodeBlockDtoBuilder.create()
                                                    .codeFormat("builder.apply();")
                                                    .build()))))
            .build();

    assertGeneratedSource(
        """
        package test;
        public class DocBuilder {
          /**
           * Applies the values.
           <h4>Example:</h4><pre>{@code
           * builder.apply();
           * }</pre>
           * @since
           */
          public void apply() {
          }
        }""",
        classDef);
  }

  @Test
  void generatesNestedInterfaceWithAbstractMethod() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "NestedBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .nestedTypes(
                nestedTypes ->
                    nestedTypes.add(
                        n ->
                            n.typeName("Callback")
                                .kind(NestedTypeDto.NestedTypeKind.INTERFACE)
                                .methods(
                                    methods ->
                                        methods.add(
                                            m ->
                                                m.methodName("onDone")
                                                    .returnType(TypeNamePrimitive.VOID)))))
            .build();

    assertGeneratedSource(
        """
        package test;
        public class NestedBuilder {
          public interface Callback {
            public abstract void onDone() {
            }
          }
        }""",
        classDef);
  }

  @Test
  void generatesConstructorAndFieldVariants() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "CtorBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .classFields(
                fields ->
                    fields.add(
                        f ->
                            f.fieldName("DEFAULT_SIZE")
                                .fieldType(TypeNamePrimitive.INT)
                                .visibility(AccessModifier.PRIVATE)
                                .literalInitializer("42")))
            .constructors(
                constructors ->
                    constructors.add(
                        c ->
                            c.visibility(AccessModifier.PROTECTED)
                                .parameters(
                                    parameters ->
                                        parameters.add(
                                            p ->
                                                p.parameterName("capacity")
                                                    .parameterTypeName(TypeNamePrimitive.INT)
                                                    .annotations(
                                                        annotations ->
                                                            annotations.add(
                                                                a ->
                                                                    a.annotationType(
                                                                        "test.anno",
                                                                        "NonNull")))))))
            .build();

    assertGeneratedSource(
        """
        package test;
        import test.anno.NonNull;
        public class CtorBuilder {
          private int DEFAULT_SIZE = 42;
          protected CtorBuilder(int capacity) {
          }
        }""",
        classDef);
  }

  @Test
  void generatesNestedTypesWithMembers() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "OuterBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .nestedTypes(
                nestedTypes ->
                    nestedTypes.add(
                        n ->
                            n.typeName("Helper")
                                .kind(NestedTypeDto.NestedTypeKind.CLASS)
                                .visibility(AccessModifier.PUBLIC)
                                .javadoc(javadoc -> javadoc.format("Nested helper.").args())
                                .methods(
                                    methods ->
                                        methods.add(
                                            m ->
                                                m.methodName("assist")
                                                    .returnType(TypeNamePrimitive.VOID)
                                                    .modifier(AccessModifier.PRIVATE)
                                                    .code("// nothing")))))
            .build();

    assertGeneratedSource(
        """
        package test;
        public class OuterBuilder {
          /**
           * Nested helper.
           */
          public class Helper {
            public void assist() {
            }
          }
        }""",
        classDef);
  }

  @Test
  void generatesMethodWithJavadocAnnotationsAndTypedParameters() throws Exception {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("test", "RichBuilder"))
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .methods(
                methods ->
                    methods.add(
                        m ->
                            m.methodName("items")
                                .returnTypeGeneric(List.class, String.class)
                                .modifier(AccessModifier.PUBLIC)
                                .javadoc(
                                    javadoc ->
                                        javadoc
                                            .format("Adds items.")
                                            .args()
                                            .add2Tags(
                                                new JavadocTagDto("param", "values items to add"))
                                            .add2Tags(new JavadocTagDto("return", "this builder")))
                                .code("return null;")
                                .annotations(
                                    annotations ->
                                        annotations.add(a -> a.annotationType(Override.class)))
                                .parameters(
                                    parameters ->
                                        parameters.add(
                                            p ->
                                                p.parameterName("values")
                                                    .parameterTypeNameGeneric(
                                                        List.class, String.class)))))
            .build();

    assertGeneratedSource(
        """
        package test;
        import java.util.List;
        public class RichBuilder {
          /**
           * Adds items.
           * @param values items to add
           * @return this builder
           */
          @Override
          public List<String> items(List<String> values) {
            return null;
          }
        }""",
        classDef);
  }

  /**
   * Generates {@code classDef} into a fresh {@link TestingGenerationEnvironment} and returns it.
   */
  private static TestingGenerationEnvironment generate(GenerationTargetClassDto classDef)
      throws Exception {
    TestingGenerationEnvironment env = TestingGenerationEnvironment.create();
    new RoasterCodeGenerator(env)
        .generateClass(classDef, new RoasterSourceFormatter(env.getLogger(), FormattingMode.NONE));
    return env;
  }

  /** Generates {@code classDef} and compares the emitted source against {@code expected}. */
  private static void assertGeneratedSource(String expected, GenerationTargetClassDto classDef)
      throws Exception {
    assertNormalizedEquals(
        expected,
        generate(classDef).generatedSource(),
        "generated " + classDef.getTypeName().getClassName() + " must match the expected source");
  }

  /** A builder-style DTO: private target field, one delegating setter, one build method. */
  private static GenerationTargetClassDto personBuilderDefinition() {
    TypeName builderType = new TypeName("test", "PersonBuilder");
    TypeName personType = new TypeName("test", "Person");

    return GenerationTargetClassDtoBuilder.create()
        .typeName(builderType)
        .classAccessModifier(AccessModifier.PUBLIC)
        .formattingMode(FormattingMode.NONE)
        .classFields(
            fields ->
                fields.add(
                    f ->
                        f.fieldName("target")
                            .fieldType(personType)
                            .visibility(AccessModifier.PRIVATE)
                            .literalInitializer("new Person()")))
        .methods(
            methods ->
                methods
                    .add(
                        m ->
                            m.methodName("name")
                                .returnType(builderType)
                                .modifier(AccessModifier.PUBLIC)
                                .code(
                                    """
                                    target.setName(name);
                                    return this;""")
                                .parameters(
                                    parameters ->
                                        parameters.add(
                                            p ->
                                                p.parameterName("name")
                                                    .parameterTypeName(String.class))))
                    .add(
                        m ->
                            m.methodName("build")
                                .returnType(personType)
                                .modifier(AccessModifier.PUBLIC)
                                .code("return target;")))
        .build();
  }

  private static GenerationTargetClassDto annotatedClassDefinition() {
    return GenerationTargetClassDtoBuilder.create()
        .typeName(new TypeName("test", "AnnotatedTypes"))
        .classAccessModifier(AccessModifier.PUBLIC)
        .formattingMode(FormattingMode.NONE)
        .build();
  }

  private static TypeName stringTypeNullableAnnotated() {
    TypeName type = TypeName.of(String.class);
    type.addAnnotation(
        AnnotationDtoBuilder.create().annotationType("test.anno", "Nullable").build());
    return type;
  }

  private static void assertAnnotatedSource(String expected, GenerationTargetClassDto classDef)
      throws Exception {
    String source = generate(classDef).generatedSource();
    assertNormalizedEquals(expected, source, "annotated declarations must be preserved");
    Compilation compilation =
        CodegenTestUtils.createCompiler()
            .compile(
                JavaFileObjects.forSourceString("test.AnnotatedTypes", source),
                JavaFileObjects.forSourceString(
                    "test.anno.Nullable",
                    """
                    package test.anno;
                    import java.lang.annotation.ElementType;
                    import java.lang.annotation.Target;
                    @Target(ElementType.TYPE_USE)
                    public @interface Nullable {
                    }
                    """));
    assertEquals(
        Compilation.Status.SUCCESS, compilation.status(), compilation.diagnostics().toString());
  }
}
