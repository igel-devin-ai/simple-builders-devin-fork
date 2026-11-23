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
import static org.javahelpers.codegen.testing.ElementTestUtils.method;
import static org.javahelpers.codegen.testing.ElementTestUtils.parameter;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.javahelpers.codegen.testing.ProcessingTestUtils.context;
import static org.javahelpers.codegen.testing.ProcessingTestUtils.probe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.javahelpers.codegen.analysis.JavaLangAnalyser.ReadableProperty;
import org.javahelpers.codegen.analysis.testannotations.Tag;
import org.javahelpers.codegen.analysis.testannotations.Tags;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.processing.AnnotationProcessingContext;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link JavaLangAnalyser} against elements captured from a real compilation. */
class JavaLangAnalyserTest {

  private static final String DTO_SOURCE =
      """
      package test;
      import org.javahelpers.codegen.analysis.testannotations.Tag;
      public class Dto {
        private String name;
        private boolean active;
        private int count;
        public Dto() {}
        public Dto(String name) {}
        public String getName() { return name; }
        public boolean isActive() { return active; }
        public void setName(String name) { this.name = name; }
        public void setName(Object name) {}
        private void hidden() {}
        public static String staticUtil(String in) { return in; }
        public static void staticVoid(String in) {}
        public <T> T generic(T in) { return in; }
        public void risky() throws java.io.IOException {}
        @Deprecated public void old() {}
        @Tag("a") public void singleTag() {}
        @Tag("a") @Tag("b") public void tagged() {}
        @org.javahelpers.codegen.analysis.testannotations.Tags({@Tag("c"), @Tag("d")})
        public void wrapped() {}
      }
      """;

  private static final String RECORD_SOURCE =
      """
      package test;
      public record Point(int x, int y) {}
      """;

  private static final String INTERFACES_SOURCE =
      """
      package test;
      public class Interfaces {
        public interface Single { void run(); }
        public interface Two { void a(); void b(); }
        @FunctionalInterface
        public interface Explicit { void apply(); default void extra() {} }
      }
      """;

  private static final String CTOR_SOURCE =
      """
      package test;
      public class TwoCtor {
        public TwoCtor() {}
        public TwoCtor(String a) {}
        @Deprecated
        public TwoCtor(String a, int b) {}
      }
      """;

  private static final String PRIVATE_CTOR_SOURCE =
      """
      package test;
      public class PrivateCtor {
        private PrivateCtor() {}
        public PrivateCtor(String a) {}
      }
      """;

  private static final String NESTED_SOURCE =
      """
      package test;
      public class Outer {
        public static class Inner {
          public static class Deep {}
        }
      }
      """;

  private static final String KEYWORD_DTO_SOURCE =
      """
      package test;
      public class KeywordDto {
        public boolean isStatic() { return true; }
        public void setStatic(boolean v) {}
        public void setTransient(int v) {}
      }
      """;

  private static final String GETTER_STYLE_SOURCE =
      """
      package test;
      public class MultiGetterDto {
        private boolean active;
        public boolean active() { return active; }
        public boolean getActive() { return active; }
        public boolean isActive() { return active; }
        public void setActive(boolean v) { this.active = v; }
      }
      """;

  @Test
  void isNoMethodOfObjectClass_filtersObjectMethods() {
    AtomicReference<Boolean> onObject = new AtomicReference<>();
    AtomicReference<Boolean> onOwn = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          onObject.set(
              JavaLangAnalyser.isNoMethodOfObjectClass(
                  method(typeElement(env, "java.lang.Object"), "toString"), ctx));
          onOwn.set(JavaLangAnalyser.isNoMethodOfObjectClass(method(dto, "getName"), ctx));
        },
        DTO_SOURCE);
    assertFalse(onObject.get());
    assertTrue(onOwn.get());
  }

  @Test
  void findSetterMethods_returnsSettersSortedByNameAndParameterType() {
    AtomicReference<List<String>> result = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          result.set(
              JavaLangAnalyser.findSetterMethods(typeElement(env, "test.Dto"), ctx).stream()
                  .map(m -> m.getSimpleName() + "(" + m.getParameters().get(0).asType() + ")")
                  .toList());
        },
        DTO_SOURCE);
    assertEquals(List.of("setName(java.lang.Object)", "setName(java.lang.String)"), result.get());
  }

  @Test
  void hasNotAnnotation_checksForAnnotationPresence() {
    AtomicReference<Boolean> deprecated = new AtomicReference<>();
    AtomicReference<Boolean> plain = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          TypeElement dto = typeElement(env, "test.Dto");
          deprecated.set(JavaLangAnalyser.hasNotAnnotation(Deprecated.class, method(dto, "old")));
          plain.set(JavaLangAnalyser.hasNotAnnotation(Deprecated.class, method(dto, "getName")));
        },
        DTO_SOURCE);
    assertFalse(deprecated.get());
    assertTrue(plain.get());
  }

  @Test
  void methodPredicates_coverThrowsReturnVisibilityStaticGenerics() {
    AtomicReference<List<Boolean>> results = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          TypeElement dto = typeElement(env, "test.Dto");
          results.set(
              List.of(
                  JavaLangAnalyser.hasNoThrowablesDeclared(method(dto, "getName")),
                  JavaLangAnalyser.hasNoThrowablesDeclared(method(dto, "risky")),
                  // setName returns void -> no return value; getName returns String
                  JavaLangAnalyser.hasNoReturnValue(method(dto, "setName", "java.lang.String")),
                  JavaLangAnalyser.hasNoReturnValue(method(dto, "getName")),
                  JavaLangAnalyser.isNotPrivate(method(dto, "getName")),
                  JavaLangAnalyser.isNotPrivate(method(dto, "hidden")),
                  JavaLangAnalyser.isNotStatic(method(dto, "getName")),
                  JavaLangAnalyser.isNotStatic(method(dto, "staticUtil")),
                  JavaLangAnalyser.hasGenericTypes(method(dto, "generic")),
                  JavaLangAnalyser.hasGenericTypes(method(dto, "getName")),
                  JavaLangAnalyser.isSetterForField(method(dto, "setName", "java.lang.String")),
                  JavaLangAnalyser.isSetterForField(method(dto, "getName"))));
        },
        DTO_SOURCE);
    assertEquals(
        List.of(true, false, true, false, true, false, true, false, true, false, true, false),
        results.get());
  }

  @Test
  void hasEmptyConstructor_detectsAccessibleNoArgConstructor() {
    AtomicReference<Boolean> dto = new AtomicReference<>();
    AtomicReference<Boolean> ctor = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          dto.set(JavaLangAnalyser.hasEmptyConstructor(typeElement(env, "test.Dto"), ctx));
          // TwoCtor has public no-arg ctor -> true as well
          ctor.set(JavaLangAnalyser.hasEmptyConstructor(typeElement(env, "test.TwoCtor"), ctx));
        },
        DTO_SOURCE,
        CTOR_SOURCE);
    assertTrue(dto.get());
    assertTrue(ctor.get());
  }

  @Test
  void findAnnotation_locatesDirectAnnotation() {
    AtomicReference<Optional<AnnotationMirror>> result = new AtomicReference<>();
    probe(
        (roundEnv, env) ->
            result.set(
                JavaLangAnalyser.findAnnotation(
                    method(typeElement(env, "test.Dto"), "old"), Deprecated.class)),
        DTO_SOURCE);
    assertTrue(result.get().isPresent());
  }

  @Test
  void findAnnotationInstances_unwrapsRepeatableContainerAndDirect() {
    AtomicReference<Integer> directCount = new AtomicReference<>();
    AtomicReference<Integer> containerCount = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          directCount.set(
              JavaLangAnalyser.findAnnotationInstances(
                      method(dto, "tagged"), Tag.class, Tags.class, ctx)
                  .size());
          containerCount.set(
              JavaLangAnalyser.findAnnotationInstances(
                      method(dto, "wrapped"), Tag.class, Tags.class, ctx)
                  .size());
        },
        DTO_SOURCE);
    assertEquals(2, directCount.get());
    assertEquals(2, containerCount.get());
  }

  @Test
  void findAnnotationAttributeAndArray_readValues() {
    AtomicReference<String> value = new AtomicReference<>();
    AtomicReference<Integer> arraySize = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          AnnotationMirror tag =
              JavaLangAnalyser.findAnnotation(method(dto, "singleTag"), Tag.class).orElseThrow();
          value.set(
              JavaLangAnalyser.findAnnotationAttribute(tag, "value", ctx)
                  .map(AnnotationValue::getValue)
                  .map(Object::toString)
                  .orElse(null));
          AnnotationMirror tags =
              JavaLangAnalyser.findAnnotation(method(dto, "wrapped"), Tags.class).orElse(null);
          if (tags != null) {
            arraySize.set(JavaLangAnalyser.findAnnotationAttributeArray(tags, "value", ctx).size());
          }
        },
        DTO_SOURCE);
    assertEquals("a", value.get());
    assertEquals(2, arraySize.get());
  }

  @Test
  void resolveTypeElement_resolvesAnnotationValueToTypeElement() {
    AtomicReference<Optional<TypeElement>> result = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          AnnotationMirror tag =
              JavaLangAnalyser.findAnnotation(method(dto, "singleTag"), Tag.class).orElseThrow();
          // value is a string, not a TypeMirror -> empty
          AnnotationValue val =
              JavaLangAnalyser.findAnnotationAttribute(tag, "value", ctx).orElseThrow();
          result.set(JavaLangAnalyser.resolveTypeElement(val, ctx));
        },
        DTO_SOURCE);
    assertTrue(result.get().isEmpty());
  }

  @Test
  void findMethodsStatic_filtersBySignatureAndStaticModifier() {
    AtomicReference<List<String>> single = new AtomicReference<>();
    AtomicReference<List<String>> voidReturn = new AtomicReference<>();
    AtomicReference<List<String>> allStatic = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          single.set(
              JavaLangAnalyser.findMethodsStatic(
                      dto, TypeName.of(String.class), TypeName.of(String.class), ctx)
                  .stream()
                  .map(m -> m.getSimpleName().toString())
                  .toList());
          voidReturn.set(
              JavaLangAnalyser.findMethodsStatic(dto, List.of(TypeName.of(String.class)), null, ctx)
                  .stream()
                  .map(m -> m.getSimpleName().toString())
                  .toList());
          allStatic.set(
              JavaLangAnalyser.findMethodsStatic(dto, List.of(TypeName.of(String.class)), ctx)
                  .stream()
                  .map(m -> m.getSimpleName().toString())
                  .toList());
        },
        DTO_SOURCE);
    assertEquals(List.of("staticUtil"), single.get());
    assertEquals(List.of("staticVoid"), voidReturn.get());
    assertEquals(List.of("staticUtil", "staticVoid"), allStatic.get());
  }

  @Test
  void findMethods_matchesByParametersAndReturnType() {
    AtomicReference<List<String>> result = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          result.set(
              JavaLangAnalyser.findMethods(dto, List.of(), TypeName.of(String.class), ctx).stream()
                  .map(m -> m.getSimpleName().toString())
                  .toList());
        },
        DTO_SOURCE);
    assertEquals(List.of("toString", "getName"), result.get());
  }

  @Test
  void findMethodWithoutParameters_matchesNameAndModifiers() {
    AtomicReference<Optional<ExecutableElement>> found = new AtomicReference<>();
    AtomicReference<Optional<ExecutableElement>> missing = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          TypeElement dto = typeElement(env, "test.Dto");
          found.set(JavaLangAnalyser.findMethodWithoutParameters(dto, "getName", Modifier.PUBLIC));
          missing.set(
              JavaLangAnalyser.findMethodWithoutParameters(dto, "setName", Modifier.PUBLIC));
        },
        DTO_SOURCE);
    assertTrue(found.get().isPresent());
    assertTrue(missing.get().isEmpty());
  }

  @Test
  void findReadableProperties_combinesRecordComponentsGettersAndFields() {
    AtomicReference<List<String>> recordProps = new AtomicReference<>();
    AtomicReference<List<String>> dtoProps = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          recordProps.set(
              JavaLangAnalyser.findReadableProperties(typeElement(env, "test.Point"), ctx).stream()
                  .map(ReadableProperty::name)
                  .toList());
          dtoProps.set(
              JavaLangAnalyser.findReadableProperties(typeElement(env, "test.Dto"), ctx).stream()
                  .map(ReadableProperty::name)
                  .toList());
        },
        RECORD_SOURCE,
        DTO_SOURCE);
    assertEquals(List.of("x", "y"), recordProps.get());
    // getters are readable; private fields without accessors are not
    assertTrue(dtoProps.get().containsAll(List.of("name", "active")));
  }

  @Test
  void findSingleParameterMethods_returnsNonStaticOneParamMethods() {
    AtomicReference<List<String>> result = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          result.set(
              JavaLangAnalyser.findSingleParameterMethods(typeElement(env, "test.Dto"), ctx)
                  .stream()
                  .map(m -> m.getSimpleName().toString())
                  .distinct()
                  .sorted()
                  .toList());
        },
        DTO_SOURCE);
    // setName overloads + generic — staticUtil excluded (static)
    assertEquals(List.of("generic", "setName"), result.get());
  }

  @Test
  void hasConstructorAccepting_matchesParameterType() {
    AtomicReference<Boolean> matching = new AtomicReference<>();
    AtomicReference<Boolean> nonMatching = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement ctor = typeElement(env, "test.TwoCtor");
          matching.set(
              JavaLangAnalyser.hasConstructorAccepting(ctor, TypeName.of(String.class), ctx));
          nonMatching.set(
              JavaLangAnalyser.hasConstructorAccepting(ctor, TypeName.of(Integer.class), ctx));
        },
        CTOR_SOURCE);
    assertTrue(matching.get());
    assertFalse(nonMatching.get());
  }

  @Test
  void isFunctionalInterface_detectsAnnotatedAndSingleAbstractMethod() {
    AtomicReference<List<Boolean>> results = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          results.set(
              List.of(
                  JavaLangAnalyser.isFunctionalInterface(
                      typeElement(env, "test.Interfaces.Single")),
                  JavaLangAnalyser.isFunctionalInterface(typeElement(env, "test.Interfaces.Two")),
                  JavaLangAnalyser.isFunctionalInterface(
                      typeElement(env, "test.Interfaces.Explicit")),
                  JavaLangAnalyser.isFunctionalInterface(typeElement(env, "test.Dto")),
                  JavaLangAnalyser.isFunctionalInterface(null)));
        },
        INTERFACES_SOURCE,
        DTO_SOURCE);
    assertEquals(List.of(true, false, true, false, false), results.get());
  }

  @Test
  void extractParamJavaDoc_readsParamTextWithContinuation() {
    AtomicReference<ExecutableElement> mth = new AtomicReference<>();
    probe(
        (roundEnv, env) ->
            mth.set(method(typeElement(env, "test.Dto"), "setName", "java.lang.String")),
        DTO_SOURCE);
    String javadoc =
        """
        Sets the name.
        @param name the name
        continued description
        @return nothing
        """;
    assertEquals(
        "the name continued description",
        JavaLangAnalyser.extractParamJavaDoc(javadoc, parameter(mth.get(), 0)));
    assertNull(JavaLangAnalyser.extractParamJavaDoc(null, parameter(mth.get(), 0)));
    assertNull(JavaLangAnalyser.extractParamJavaDoc(javadoc, null));
  }

  @Test
  void extractDeprecatedJavaDoc_readsDeprecatedTagText() {
    String javadoc = "Old method.\n@deprecated use {@code fresh()} instead\n@return x";
    assertEquals("use {@code fresh()} instead", JavaLangAnalyser.extractDeprecatedJavaDoc(javadoc));
    assertNull(JavaLangAnalyser.extractDeprecatedJavaDoc(null));
    assertNull(JavaLangAnalyser.extractDeprecatedJavaDoc("no tags here"));
  }

  @Test
  void findGetterForField_matchesRecordBooleanAndStandardAccessors() {
    AtomicReference<Optional<ExecutableElement>> byGet = new AtomicReference<>();
    AtomicReference<Optional<ExecutableElement>> byIs = new AtomicReference<>();
    AtomicReference<Optional<ExecutableElement>> absent = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          byGet.set(
              JavaLangAnalyser.findGetterForField(dto, "name", field(dto, "name").asType(), ctx));
          byIs.set(
              JavaLangAnalyser.findGetterForField(
                  dto, "active", field(dto, "active").asType(), ctx));
          absent.set(
              JavaLangAnalyser.findGetterForField(dto, "count", field(dto, "count").asType(), ctx));
        },
        DTO_SOURCE);
    assertTrue(byGet.get().isPresent());
    assertTrue(byIs.get().isPresent());
    assertTrue(absent.get().isEmpty());
  }

  @Test
  void findFieldElementAndRecordComponent_locateMembers() {
    AtomicReference<Boolean> fieldFound = new AtomicReference<>();
    AtomicReference<Boolean> fieldMissing = new AtomicReference<>();
    AtomicReference<Boolean> compFound = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          TypeElement dto = typeElement(env, "test.Dto");
          fieldFound.set(JavaLangAnalyser.findFieldElement(dto, "name").isPresent());
          fieldMissing.set(JavaLangAnalyser.findFieldElement(dto, "missing").isEmpty());
          compFound.set(
              JavaLangAnalyser.findRecordComponent(typeElement(env, "test.Point"), "x")
                  .isPresent());
        },
        DTO_SOURCE,
        RECORD_SOURCE);
    assertTrue(fieldFound.get());
    assertTrue(fieldMissing.get());
    assertTrue(compFound.get());
  }

  @Test
  void setterDiscoveryAndPropertyResolutionUseTheSameNamePredicate() {
    AtomicReference<List<String>> discovered = new AtomicReference<>();
    AtomicReference<Optional<String>> resolved = new AtomicReference<>();
    AtomicReference<Optional<ExecutableElement>> nonSetter = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.SetterDto");
          discovered.set(
              JavaLangAnalyser.findSetterMethods(dto, ctx).stream()
                  .map(m -> m.getSimpleName().toString())
                  .toList());
          resolved.set(
              JavaLangAnalyser.findSetterForProperty(
                      dto,
                      "isStatic",
                      method(dto, "setStatic").getParameters().get(0).asType(),
                      ctx)
                  .map(m -> m.getSimpleName().toString()));
          nonSetter.set(
              JavaLangAnalyser.findSetterForProperty(
                  dto, "ignored", method(dto, "withIgnored").getParameters().get(0).asType(), ctx));
        },
        """
        package test;
        public class SetterDto {
          public boolean isStatic() { return false; }
          public void setStatic(boolean value) {}
          public String setTitle(String value) { return value; }
          public void withIgnored(String value) {}
          public void ignored(String value) {}
          public void setignored(String value) {}
          public void setNoArgs() {}
          public void setTwoArgs(String first, String second) {}
        }
        """);
    assertEquals(List.of("setStatic", "setTitle"), discovered.get());
    assertEquals(Optional.of("setStatic"), resolved.get());
    assertEquals(Optional.empty(), nonSetter.get());
  }

  @Test
  void findSetterForPropertyReturnsEmptyForEachNullArgument() {
    AtomicReference<List<Optional<ExecutableElement>>> results = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          var fieldType = field(dto, "name").asType();
          results.set(
              List.of(
                  JavaLangAnalyser.findSetterForProperty(null, "name", fieldType, ctx),
                  JavaLangAnalyser.findSetterForProperty(dto, null, fieldType, ctx),
                  JavaLangAnalyser.findSetterForProperty(dto, "name", null, ctx)));
        },
        DTO_SOURCE);
    assertEquals(List.of(Optional.empty(), Optional.empty(), Optional.empty()), results.get());
  }

  @Test
  void findSetterForField_matchesVoidSingleParamSetter() {
    AtomicReference<Optional<ExecutableElement>> found = new AtomicReference<>();
    AtomicReference<Optional<ExecutableElement>> missing = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.Dto");
          found.set(JavaLangAnalyser.findSetterForField(dto, "name", ctx));
          missing.set(JavaLangAnalyser.findSetterForField(dto, "count", ctx));
        },
        DTO_SOURCE);
    assertTrue(found.get().isPresent());
    assertTrue(missing.get().isEmpty());
  }

  @Test
  void resolveSetterPropertyName_usesGetterDerivedNameOrEmpty() {
    AtomicReference<Optional<String>> fromGetter = new AtomicReference<>();
    AtomicReference<Optional<String>> withoutGetter = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.KeywordDto");
          fromGetter.set(
              JavaLangAnalyser.resolveSetterPropertyName(method(dto, "setStatic"), dto, ctx));
          withoutGetter.set(
              JavaLangAnalyser.resolveSetterPropertyName(method(dto, "setTransient"), dto, ctx));
        },
        KEYWORD_DTO_SOURCE);
    assertEquals(Optional.of("isStatic"), fromGetter.get());
    assertEquals(Optional.empty(), withoutGetter.get());
  }

  @Test
  void findGetterForField_prefersIsGetThenAccessorStyle() {
    AtomicReference<Optional<ExecutableElement>> winner = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          TypeElement dto = typeElement(env, "test.MultiGetterDto");
          winner.set(
              JavaLangAnalyser.findGetterForField(
                  dto, "active", field(dto, "active").asType(), ctx));
        },
        GETTER_STYLE_SOURCE);
    assertTrue(winner.get().isPresent());
    assertEquals("isActive", winner.get().get().getSimpleName().toString());
  }

  @Test
  void findAccessibleConstructors_excludesPrivate() {
    AtomicReference<Integer> twoCtor = new AtomicReference<>();
    AtomicReference<Integer> privateCtor = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          AnnotationProcessingContext ctx = context(env);
          twoCtor.set(
              JavaLangAnalyser.findAccessibleConstructors(typeElement(env, "test.TwoCtor"), ctx)
                  .size());
          privateCtor.set(
              JavaLangAnalyser.findAccessibleConstructors(typeElement(env, "test.PrivateCtor"), ctx)
                  .size());
        },
        CTOR_SOURCE,
        PRIVATE_CTOR_SOURCE);
    assertEquals(3, twoCtor.get());
    assertEquals(1, privateCtor.get());
  }

  @Test
  void findConstructorsAnnotatedWith_findsMarkedCtor() {
    AtomicReference<List<ExecutableElement>> annotated = new AtomicReference<>();
    probe(
        (roundEnv, env) ->
            annotated.set(
                JavaLangAnalyser.findConstructorsAnnotatedWith(
                    typeElement(env, "test.TwoCtor"), Deprecated.class, context(env))),
        CTOR_SOURCE);
    assertEquals(1, annotated.get().size());
    assertEquals(2, annotated.get().get(0).getParameters().size());
  }

  @Test
  void findConstructorsAnnotatedWith_emptyWhenUnmarked() {
    AtomicReference<List<ExecutableElement>> annotated = new AtomicReference<>();
    probe(
        (roundEnv, env) ->
            annotated.set(
                JavaLangAnalyser.findConstructorsAnnotatedWith(
                    typeElement(env, "test.Dto"), Deprecated.class, context(env))),
        DTO_SOURCE);
    assertTrue(annotated.get().isEmpty());
  }

  @Test
  void findTopLevelType_walksToOutermostType() {
    AtomicReference<String> deepTop = new AtomicReference<>();
    AtomicReference<String> outerTop = new AtomicReference<>();
    probe(
        (roundEnv, env) -> {
          deepTop.set(
              JavaLangAnalyser.findTopLevelType(typeElement(env, "test.Outer.Inner.Deep"))
                  .getSimpleName()
                  .toString());
          outerTop.set(
              JavaLangAnalyser.findTopLevelType(typeElement(env, "test.Outer"))
                  .getSimpleName()
                  .toString());
        },
        NESTED_SOURCE);
    assertEquals("Outer", deepTop.get());
    assertEquals("Outer", outerTop.get());
  }
}
