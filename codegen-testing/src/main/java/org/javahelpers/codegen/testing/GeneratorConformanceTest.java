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

package org.javahelpers.codegen.testing;

import static org.javahelpers.codegen.testing.ProcessorAsserts.assertContaining;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.javahelpers.codegen.ClassCodeGenerator;
import org.javahelpers.codegen.CodegenBackend;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.core.ClassFieldDto;
import org.javahelpers.codegen.model.core.ClassFieldDtoBuilder;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.method.ConstructorDtoBuilder;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.method.MethodParameterDtoBuilder;
import org.javahelpers.codegen.model.type.GenericParameterDtoBuilder;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.NestedTypeDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Shared conformance suite for {@link ClassCodeGenerator} implementations.
 *
 * <p>Backends under test are discovered via {@link ServiceLoader}: each backend module registers a
 * {@link TestBackendProvider} on its test classpath (see {@link TestBackendProvider} for the
 * contract). Every test in this class then runs once per discovered backend, with the provider's
 * {@link TestBackendProvider#name() name} in the test display name — so a failure always identifies
 * the failing generator. New backends (issue #366) gain identical coverage without writing or
 * copying any test.
 *
 * <p>Each fixture is asserted on two levels:
 *
 * <ul>
 *   <li><b>Compilability</b> — the emitted source is fed through {@code javac} and must compile
 *       without errors
 *   <li><b>Semantic content</b> — backend-independent fragments (declaration kinds, member names,
 *       disambiguated type references), not exact formatting
 * </ul>
 *
 * <p>Backend modules must configure surefire's {@code dependenciesToScan} to include {@code
 * codegen-testing} so this class is discovered from the dependency jar.
 */
public class GeneratorConformanceTest {

  private static final String PACKAGE = "it.conformance";
  private static final Path WORK_DIR = Path.of("target", "generator-conformance");

  /**
   * Provides all backends registered via {@link ServiceLoader}, named for the test display.
   *
   * @return one named argument per discovered {@link TestBackendProvider}
   */
  static Stream<Arguments> backends() {
    return ServiceLoader.load(TestBackendProvider.class).stream()
        .map(provider -> Arguments.of(Named.of(provider.get().name(), provider.get())));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void simpleClassWithFieldsMethodsAndConstructor_compiles(TestBackendProvider backend)
      throws Exception {
    GenerationTargetClassDto classDef = baseClass("SimpleClass");

    addField(classDef, "name", TypeName.of(String.class));
    addField(classDef, "age", TypeNamePrimitive.INT);
    addField(classDef, "active", TypeNamePrimitive.BOOLEAN);

    var ctor = ConstructorDtoBuilder.create().visibility(AccessModifier.PUBLIC).build();
    ctor.getMethodCodeDto().setCodeFormat("this.name = name;");
    ctor.addParameter(param("name", TypeName.of(String.class)));
    classDef.addConstructor(ctor);

    classDef.addMethod(
        MethodDtoBuilder.create()
            .methodName("describe")
            .returnType(TypeName.of(String.class))
            .modifier(AccessModifier.PUBLIC)
            .code("return name + \"#\" + age;")
            .build());

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source, "public class SimpleClass", "String describe(", "SimpleClass(String name)");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void genericClassAndMethodBounds_compile(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("GenericBox");
    var generic = GenericParameterDtoBuilder.create().name("T").build();
    generic.addUpperBound(
        new TypeNameGeneric(TypeName.of(Comparable.class), new TypeNameVariable("T")));
    classDef.addGeneric(generic);

    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("pick")
            .returnType(new TypeNameVariable("R"))
            .modifier(AccessModifier.PUBLIC)
            .isStatic(true)
            .code("return first;")
            .build();
    var methodGeneric = GenericParameterDtoBuilder.create().name("R").build();
    methodGeneric.addUpperBound(TypeName.of(Number.class));
    method.addGenericParameter(methodGeneric);
    method.addParameter(param("first", new TypeNameVariable("R")));
    method.addParameter(param("second", new TypeNameVariable("R")));
    classDef.addMethod(method);

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(source, "class GenericBox<T extends Comparable<T>>", "<R extends Number>");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void collectionArrayAndOptionalTypes_compile(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("ContainerClass");

    addField(classDef, "names", new TypeNameGeneric(List.class, String.class));
    addField(
        classDef,
        "scores",
        new TypeNameGeneric(
            TypeName.of(java.util.Map.class),
            TypeName.of(String.class),
            TypeName.of(Integer.class)));
    addField(classDef, "numbers", new TypeNameArray(TypeNamePrimitive.INT));
    addField(classDef, "nickname", new TypeNameGeneric(java.util.Optional.class, String.class));

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(
        source, "List<String> names", "Map<String, Integer> scores", "Optional<String> nickname");
  }

  /**
   * Two field types sharing the simple name {@code Date} from different packages must never be
   * rendered ambiguously — at least one of them has to stay fully qualified (issue #417).
   */
  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void collidingSimpleNames_areDisambiguated(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("CollidingDates");

    addField(classDef, "utilDate", new TypeName("java.util", "Date"));
    addField(classDef, "sqlDate", new TypeName("java.sql", "Date"));

    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("touch")
            .returnType(TypeNamePrimitive.VOID)
            .modifier(AccessModifier.PUBLIC)
            .code("if (when == null) { return; }")
            .build();
    method.addParameter(param("when", new TypeName("java.sql", "Date")));
    classDef.addMethod(method);

    String source = generate(backend, classDef);
    assertTrue(
        source.contains("java.util.Date") || source.contains("java.sql.Date"),
        () -> "colliding Date types must be qualified in the generated source, was:\n" + source);
    assertCompiles(classDef, source);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void nestedTypesWithMembers_compile(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("OuterClass");

    NestedTypeDto nested =
        NestedTypeDtoBuilder.create()
            .typeName("Helper")
            .kind(NestedTypeDto.NestedTypeKind.CLASS)
            .visibility(AccessModifier.PUBLIC)
            .build();
    nested.addMethod(
        MethodDtoBuilder.create()
            .methodName("assist")
            .returnType(TypeNamePrimitive.VOID)
            .modifier(AccessModifier.PUBLIC)
            .code("int touched = 0;")
            .build());
    classDef.addNestedType(nested);

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(source, "class Helper", "void assist(");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void annotationsOnClassMembersAndTypeUse_compile(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("AnnotatedClass");

    AnnotationDto deprecated =
        AnnotationDtoBuilder.create().annotationType(TypeName.of(Deprecated.class)).build();
    deprecated.addMember("since", "\"0.9\"");
    deprecated.addMember("forRemoval", "true");
    classDef.addClassAnnotation(deprecated);

    AnnotationDto generated =
        AnnotationDtoBuilder.create()
            .annotationType(new TypeName("javax.annotation.processing", "Generated"))
            .build();
    generated.addMember("value", "\"conformance\"");
    classDef.addClassAnnotation(generated);

    TypeName annotatedString = TypeName.of(String.class);
    annotatedString
        .getAnnotations()
        .add(
            AnnotationDtoBuilder.create()
                .annotationType(TypeName.of(TestTypeUseAnnotation.class))
                .build());
    addField(classDef, "flagged", annotatedString);

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(source, "@Deprecated", "@Generated", "TestTypeUseAnnotation");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void codeTemplatePlaceholders_resolve(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("TemplateClass");

    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("parse")
            .returnType(TypeNamePrimitive.LONG)
            .modifier(AccessModifier.PUBLIC)
            .build();
    method
        .getMethodCodeDto()
        .setCodeFormat("int $l:L = 0;\n$b:B box = null;\nreturn $t:T.parseLong($s:S);");
    method.getMethodCodeDto().addArgument("l", "count");
    method.getMethodCodeDto().addArgument("b", TypeNamePrimitive.INT);
    method.getMethodCodeDto().addArgument("t", TypeName.of(Long.class));
    method.getMethodCodeDto().addArgument("s", "42");
    classDef.addMethod(method);

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(source, "int count = 0", "Integer box", "Long.parseLong(\"42\")");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void superTypeAndInterfaces_compile(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("DerivedClass");
    classDef.setSuperType(new TypeNameGeneric(java.util.ArrayList.class, String.class));
    classDef.addInterface(new InterfaceName("java.io", "Serializable"));
    classDef.addInterface(new InterfaceName("java.lang", "Cloneable"));

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    assertContaining(source, "extends ArrayList<String>", "implements Serializable, Cloneable");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void staticImportsAndStaticFactory_compile(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("FactoryClass");
    classDef.addStaticImport(java.util.Collections.class, "emptyList");

    ClassFieldDto field =
        ClassFieldDtoBuilder.create()
            .fieldName("initial")
            .fieldType(new TypeNameGeneric(List.class, String.class))
            .visibility(AccessModifier.PRIVATE)
            .literalInitializer("emptyList()")
            .build();
    classDef.addClassField(field);

    MethodDto factory =
        MethodDtoBuilder.create()
            .methodName("of")
            .returnType(TypeNamePrimitive.INT)
            .modifier(AccessModifier.PUBLIC)
            .isStatic(true)
            .code("return names.length;")
            .build();
    factory.addParameter(param("names", new TypeNameArray(TypeName.of(String.class))));
    classDef.addMethod(factory);

    String source = generate(backend, classDef);
    assertCompiles(classDef, source);
    // array types in parameter position may render as "String[]" or normalized "String..."
    assertTrue(
        source.contains("String[] names") || source.contains("String... names"),
        () -> "array parameter must be rendered as array or varargs, was:\n" + source);
    assertContaining(source, "import static");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("backends")
  void javadocIsEmittedForClassAndMembers(TestBackendProvider backend) throws Exception {
    GenerationTargetClassDto classDef = baseClass("DocumentedClass");
    classDef.setClassJavadoc(
        JavadocDtoBuilder.create().format("Documented conformance class.").args().build());

    var javadoc = JavadocDtoBuilder.create().format("Echoes the value.").args().build();
    javadoc.addParam("value", "the input");
    javadoc.addReturn("the echoed value");
    MethodDto method =
        MethodDtoBuilder.create()
            .methodName("echo")
            .returnType(TypeName.of(String.class))
            .modifier(AccessModifier.PUBLIC)
            .javadoc(javadoc)
            .code("return value;")
            .build();
    method.addParameter(param("value", TypeName.of(String.class)));
    classDef.addMethod(method);

    String source = generate(backend, classDef);
    assertContaining(source, "Documented conformance class.", "@param", "@return");
  }

  /** Generates {@code classDef} through {@code provider}'s backend and returns the source text. */
  private String generate(TestBackendProvider provider, GenerationTargetClassDto classDef)
      throws Exception {
    TestingGenerationEnvironment env = TestingGenerationEnvironment.create();
    CodegenBackend backend = provider.create(env, env.getLogger());
    backend.generator().generateClass(classDef, backend.formatterFor(FormattingMode.NONE));
    return env.generatedSource();
  }

  /**
   * Compiles {@code source} with the JDK compiler on the test classpath; fails when javac reports
   * an error.
   */
  private void assertCompiles(GenerationTargetClassDto classDef, String source) throws IOException {
    String qualifiedName = classDef.getTypeName().getFullQualifiedName();
    Path sourceFile = WORK_DIR.resolve(qualifiedName.replace('.', '/') + ".java");
    Files.createDirectories(sourceFile.getParent());
    Files.writeString(sourceFile, source, StandardCharsets.UTF_8);
    Path classes = Files.createDirectories(WORK_DIR.resolve("classes"));

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
      boolean ok =
          compiler
              .getTask(
                  null,
                  fileManager,
                  diagnostics,
                  List.of(
                      "-proc:none",
                      "-classpath",
                      System.getProperty("java.class.path"),
                      "-d",
                      classes.toString()),
                  null,
                  fileManager.getJavaFileObjects(sourceFile))
              .call();
      assertTrue(
          ok,
          () ->
              "generated source for "
                  + qualifiedName
                  + " must compile:\n"
                  + diagnostics.getDiagnostics().stream()
                      .map(Object::toString)
                      .collect(Collectors.joining("\n"))
                  + "\n--- source ---\n"
                  + source);
    }
  }

  private static GenerationTargetClassDto baseClass(String simpleName) {
    return GenerationTargetClassDtoBuilder.create()
        .typeName(new TypeName(PACKAGE, simpleName))
        .classAccessModifier(AccessModifier.PUBLIC)
        .formattingMode(FormattingMode.NONE)
        .build();
  }

  private static void addField(GenerationTargetClassDto classDef, String name, TypeName type) {
    classDef.addClassField(
        ClassFieldDtoBuilder.create()
            .fieldName(name)
            .fieldType(type)
            .visibility(AccessModifier.PRIVATE)
            .build());
  }

  private static MethodParameterDto param(String name, TypeName type) {
    return MethodParameterDtoBuilder.create().parameterName(name).parameterTypeName(type).build();
  }
}
