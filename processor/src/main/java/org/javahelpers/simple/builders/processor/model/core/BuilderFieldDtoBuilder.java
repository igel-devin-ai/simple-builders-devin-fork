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

package org.javahelpers.simple.builders.processor.model.core;

import static org.javahelpers.simple.builders.core.util.TrackedValue.changedValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.initialValue;
import static org.javahelpers.simple.builders.core.util.TrackedValue.unsetValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.core.DeprecationInfoDto;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.GenericParameterDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNameSet;
import org.javahelpers.simple.builders.core.builders.ArrayListBuilderWithElementBuilders;
import org.javahelpers.simple.builders.core.interfaces.IBuilderBase;
import org.javahelpers.simple.builders.core.util.BuilderToStringStyle;
import org.javahelpers.simple.builders.core.util.TrackedValue;

/**
 * Builder for {@code org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto with method chaining and
 * validation. Use the static {@code create()} method to obtain a new builder instance, configure
 * the desired properties using the setter methods, and then call {@code build()} to create the
 * final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * BuilderFieldDto result = BuilderFieldDtoBuilder.create()
 *     .defaultValue("example defaultValue")
 *     .defaultValue("Hello %s", "World")
 *     .defaultValue(() -> "example defaultValue")
 *     .defaultValue(sb -> sb.append("text"))
 *     .defaultValueUpdate(String::trim)
 *     .fieldGenerics(List.of(GenericParameterDtoBuilder.create().build()))
 *     .fieldGenerics(() -> List.of(GenericParameterDtoBuilder.create().build()))
 *     .fieldGenerics(t -> t.add(genericParameterDtoBuilder -> genericParameterDtoBuilder))
 *     .fieldGenerics(GenericParameterDtoBuilder.create().build(), GenericParameterDtoBuilder.create().build())
 *     .fieldGenericsUpdate(list -> list.stream().sorted().toList())
 *     .add2FieldGenerics(GenericParameterDtoBuilder.create().build())
 *     .fieldNameInBuilder("example fieldNameInBuilder")
 *     .fieldNameInBuilder("Hello %s", "World")
 *     .fieldNameInBuilder(() -> "example fieldNameInBuilder")
 *     .fieldNameInBuilder(sb -> sb.append("text"))
 *     .fieldNameInBuilderUpdate(String::trim)
 *     .getterInfo(GetterInfoDtoBuilder.create().build())
 *     .getterInfo(() -> GetterInfoDtoBuilder.create().build())
 *     .getterInfo(getterInfoDtoBuilder -> getterInfoDtoBuilder)
 *     .javaDoc(new JavadocDto())
 *     .javaDoc(JavadocDto::new)
 *     .javaDoc(javadocDtoBuilder -> javadocDtoBuilder)
 *     .nonNullable(true)
 *     .nonNullable(() -> true)
 *     .nonNullableUpdate(value -> !value)
 *     .originalFieldName("example originalFieldName")
 *     .originalFieldName("Hello %s", "World")
 *     .originalFieldName(() -> "example originalFieldName")
 *     .originalFieldName(sb -> sb.append("text"))
 *     .originalFieldNameUpdate(String::trim)
 *     .originalJavaDocDescription("example originalJavaDocDescription")
 *     .originalJavaDocDescription("Hello %s", "World")
 *     .originalJavaDocDescription(() -> "example originalJavaDocDescription")
 *     .originalJavaDocDescription(sb -> sb.append("text"))
 *     .originalJavaDocDescriptionUpdate(String::trim)
 *     .parameterAnnotations(List.of(AnnotationDtoBuilder.create().build()))
 *     .parameterAnnotations(() -> List.of(AnnotationDtoBuilder.create().build()))
 *     .parameterAnnotations(t -> t.add(annotationDtoBuilder -> annotationDtoBuilder))
 *     .parameterAnnotations(AnnotationDtoBuilder.create().build(), AnnotationDtoBuilder.create().build())
 *     .parameterAnnotationsUpdate(list -> list.stream().sorted().toList())
 *     .add2ParameterAnnotations(AnnotationDtoBuilder.create().build())
 *     .setterName("example setterName")
 *     .setterName("Hello %s", "World")
 *     .setterName(() -> "example setterName")
 *     .setterName(sb -> sb.append("text"))
 *     .setterNameUpdate(String::trim)
 *     .build();
 * }</pre>
 */
public class BuilderFieldDtoBuilder implements IBuilderBase<BuilderFieldDto> {

  /** Tracked value for <code>defaultValue</code>: defaultValue. */
  private TrackedValue<String> defaultValue = unsetValue();

  /** Tracked value for <code>deprecationInfo</code>: deprecationInfo. */
  private TrackedValue<DeprecationInfoDto> deprecationInfo = unsetValue();

  /** Tracked value for <code>fieldGenerics</code>: fieldGenerics. */
  private TrackedValue<List<GenericParameterDto>> fieldGenerics = unsetValue();

  /** Tracked value for <code>fieldNameInBuilder</code>: fieldNameInBuilder. */
  private TrackedValue<String> fieldNameInBuilder = unsetValue();

  /** Tracked value for <code>fieldType</code>: fieldType. */
  private TrackedValue<TypeName> fieldType = unsetValue();

  /** Tracked value for <code>getterInfo</code>: getterInfo. */
  private TrackedValue<GetterInfoDto> getterInfo = unsetValue();

  /** Tracked value for <code>javaDoc</code>: javaDoc. */
  private TrackedValue<JavadocDto> javaDoc = unsetValue();

  /** Tracked value for <code>nonNullable</code>: nonNullable. */
  private TrackedValue<Boolean> nonNullable = unsetValue();

  /** Tracked value for <code>originalFieldName</code>: originalFieldName. */
  private TrackedValue<String> originalFieldName = unsetValue();

  /** Tracked value for <code>originalJavaDocDescription</code>: originalJavaDocDescription. */
  private TrackedValue<String> originalJavaDocDescription = unsetValue();

  /** Tracked value for <code>parameterAnnotations</code>: parameterAnnotations. */
  private TrackedValue<List<AnnotationDto>> parameterAnnotations = unsetValue();

  /** Tracked value for <code>setterName</code>: setterName. */
  private TrackedValue<String> setterName = unsetValue();

  /**
   * Empty constructor of builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto}.
   */
  public BuilderFieldDtoBuilder() {}

  /**
   * Initialisation of builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto} by a instance.
   *
   * @param instance object instance for initialisiation
   */
  public BuilderFieldDtoBuilder(BuilderFieldDto instance) {
    this.deprecationInfo = initialValue(instance.getDeprecationInfo());
    this.fieldGenerics = initialValue(instance.getFieldGenerics());
    this.fieldNameInBuilder = initialValue(instance.getFieldNameInBuilder());
    this.fieldType = initialValue(instance.getFieldType());
    this.getterInfo = initialValue(instance.getGetterInfo());
    this.javaDoc = initialValue(instance.getJavaDoc());
    this.nonNullable = initialValue(instance.isNonNullable());
    this.originalFieldName = initialValue(instance.getOriginalFieldName());
    this.originalJavaDocDescription = initialValue(instance.getOriginalJavaDocDescription());
    this.parameterAnnotations = initialValue(instance.getParameterAnnotations());
  }

  /**
   * Creating a new builder for {@code
   * org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * BuilderFieldDtoBuilder builder = BuilderFieldDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code
   *     org.javahelpers.simple.builders.processor.model.core.BuilderFieldDto}
   */
  public static BuilderFieldDtoBuilder create() {
    return new BuilderFieldDtoBuilder();
  }

  /**
   * Sets the value for <code>defaultValue</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue(sb -> sb.append("text"));
   * }</pre>
   *
   * @param defaultValueStringBuilderConsumer consumer providing an instance of defaultValue
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder defaultValue(
      Consumer<StringBuilder> defaultValueStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    defaultValueStringBuilderConsumer.accept(builder);
    this.defaultValue = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>defaultValue</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue(() -> "example defaultValue");
   * }</pre>
   *
   * @param defaultValueSupplier supplier for defaultValue
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder defaultValue(Supplier<String> defaultValueSupplier) {
    this.defaultValue = changedValue(defaultValueSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>defaultValue</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder defaultValue(String format, Object... args) {
    this.defaultValue = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>defaultValue</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue("example defaultValue");
   * }</pre>
   *
   * @param defaultValue defaultValue
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder defaultValue(String defaultValue) {
    this.defaultValue = changedValue(defaultValue);
    return this;
  }

  /**
   * Updates the current value of <code>defaultValue</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDefaultValue(String) setDefaultValue(String
   * defaultValue)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.defaultValue("example defaultValue").defaultValueUpdate(String::trim);
   * }</pre>
   *
   * @param defaultValueUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>defaultValue</code> has not been set yet
   */
  public BuilderFieldDtoBuilder defaultValueUpdate(UnaryOperator<String> defaultValueUpdater) {
    if (!this.defaultValue.isSet()) {
      throw new IllegalStateException("Cannot update 'defaultValue' before it is set");
    }
    this.defaultValue = changedValue(defaultValueUpdater.apply(this.defaultValue.value()));
    return this;
  }

  /**
   * Sets the value for <code>deprecationInfo</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDeprecationInfo(DeprecationInfoDto)
   * setDeprecationInfo(DeprecationInfoDto deprecationInfo)}
   *
   * @param deprecationInfoSupplier supplier for deprecationInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder deprecationInfo(
      Supplier<DeprecationInfoDto> deprecationInfoSupplier) {
    this.deprecationInfo = changedValue(deprecationInfoSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>deprecationInfo</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDeprecationInfo(DeprecationInfoDto)
   * setDeprecationInfo(DeprecationInfoDto deprecationInfo)}
   *
   * @param deprecationInfo deprecationInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder deprecationInfo(DeprecationInfoDto deprecationInfo) {
    this.deprecationInfo = changedValue(deprecationInfo);
    return this;
  }

  /**
   * Updates the current value of <code>deprecationInfo</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance).
   *
   * <p>Generated from setter {@link BuilderFieldDto#setDeprecationInfo(DeprecationInfoDto)
   * setDeprecationInfo(DeprecationInfoDto deprecationInfo)}
   *
   * @param deprecationInfoUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>deprecationInfo</code> has not been set yet
   */
  public BuilderFieldDtoBuilder deprecationInfoUpdate(
      UnaryOperator<DeprecationInfoDto> deprecationInfoUpdater) {
    if (!this.deprecationInfo.isSet()) {
      throw new IllegalStateException("Cannot update 'deprecationInfo' before it is set");
    }
    this.deprecationInfo = changedValue(deprecationInfoUpdater.apply(this.deprecationInfo.value()));
    return this;
  }

  /**
   * Adds a single element to <code>fieldGenerics</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2FieldGenerics(GenericParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder add2FieldGenerics(GenericParameterDto element) {
    List<GenericParameterDto> newCollection;
    if (this.fieldGenerics.isSet()) {
      newCollection = new ArrayList<>(this.fieldGenerics.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.fieldGenerics = changedValue(newCollection);
    return this;
  }

  /**
   * Sets the value for <code>fieldGenerics</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldGenerics(GenericParameterDtoBuilder.create().build(), GenericParameterDtoBuilder.create().build());
   * }</pre>
   *
   * @param fieldGenerics fieldGenerics
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldGenerics(GenericParameterDto... fieldGenerics) {
    this.fieldGenerics = changedValue(List.of(fieldGenerics));
    return this;
  }

  /**
   * Sets the value for <code>fieldGenerics</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldGenerics(t -> t.add(genericParameterDtoBuilder -> genericParameterDtoBuilder));
   * }</pre>
   *
   * @param fieldGenericsBuilderConsumer consumer providing an instance of a builder for
   *     fieldGenerics
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldGenerics(
      Consumer<
              ArrayListBuilderWithElementBuilders<
                  GenericParameterDto, GenericParameterDtoElementBuilder>>
          fieldGenericsBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<GenericParameterDto, GenericParameterDtoElementBuilder>
        builder =
            this.fieldGenerics.isSet()
                ? new ArrayListBuilderWithElementBuilders<
                    GenericParameterDto, GenericParameterDtoElementBuilder>(
                    this.fieldGenerics.value(), GenericParameterDtoElementBuilder::new)
                : new ArrayListBuilderWithElementBuilders<
                    GenericParameterDto, GenericParameterDtoElementBuilder>(
                    GenericParameterDtoElementBuilder::new);
    fieldGenericsBuilderConsumer.accept(builder);
    this.fieldGenerics = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>fieldGenerics</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldGenerics(() -> List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param fieldGenericsSupplier supplier for fieldGenerics
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldGenerics(
      Supplier<List<GenericParameterDto>> fieldGenericsSupplier) {
    this.fieldGenerics = changedValue(fieldGenericsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>fieldGenerics</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldGenerics(List.of(GenericParameterDtoBuilder.create().build()));
   * }</pre>
   *
   * @param fieldGenerics fieldGenerics
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldGenerics(List<GenericParameterDto> fieldGenerics) {
    this.fieldGenerics = changedValue(fieldGenerics);
    return this;
  }

  /**
   * Updates the current value of <code>fieldGenerics</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance). For changing multiple elements of a nested DTO,
   * prefer the builder-consumer helper {@link #fieldGenerics(Consumer)}.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldGenerics(List)
   * setFieldGenerics(List<GenericParameterDto> fieldGenerics)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldGenerics(List.of(GenericParameterDtoBuilder.create().build()))
   *     .fieldGenericsUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param fieldGenericsUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>fieldGenerics</code> has not been set yet
   */
  public BuilderFieldDtoBuilder fieldGenericsUpdate(
      UnaryOperator<List<GenericParameterDto>> fieldGenericsUpdater) {
    if (!this.fieldGenerics.isSet()) {
      throw new IllegalStateException("Cannot update 'fieldGenerics' before it is set");
    }
    this.fieldGenerics = changedValue(fieldGenericsUpdater.apply(this.fieldGenerics.value()));
    return this;
  }

  /**
   * Sets the value for <code>fieldNameInBuilder</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder(sb -> sb.append("text"));
   * }</pre>
   *
   * @param fieldNameInBuilderStringBuilderConsumer consumer providing an instance of
   *     fieldNameInBuilder
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldNameInBuilder(
      Consumer<StringBuilder> fieldNameInBuilderStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    fieldNameInBuilderStringBuilderConsumer.accept(builder);
    this.fieldNameInBuilder = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>fieldNameInBuilder</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder(() -> "example fieldNameInBuilder");
   * }</pre>
   *
   * @param fieldNameInBuilderSupplier supplier for fieldNameInBuilder
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldNameInBuilder(Supplier<String> fieldNameInBuilderSupplier) {
    this.fieldNameInBuilder = changedValue(fieldNameInBuilderSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>fieldNameInBuilder</code> by using String.format(format, args).
   * See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldNameInBuilder(String format, Object... args) {
    this.fieldNameInBuilder = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>fieldNameInBuilder</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder("example fieldNameInBuilder");
   * }</pre>
   *
   * @param fieldNameInBuilder fieldNameInBuilder
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldNameInBuilder(String fieldNameInBuilder) {
    this.fieldNameInBuilder = changedValue(fieldNameInBuilder);
    return this;
  }

  /**
   * Updates the current value of <code>fieldNameInBuilder</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance).
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldNameInBuilder(String)
   * setFieldNameInBuilder(String fieldNameInBuilder)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.fieldNameInBuilder("example fieldNameInBuilder").fieldNameInBuilderUpdate(String::trim);
   * }</pre>
   *
   * @param fieldNameInBuilderUpdater operator applied to the current value; its result becomes the
   *     new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>fieldNameInBuilder</code> has not been set yet
   */
  public BuilderFieldDtoBuilder fieldNameInBuilderUpdate(
      UnaryOperator<String> fieldNameInBuilderUpdater) {
    if (!this.fieldNameInBuilder.isSet()) {
      throw new IllegalStateException("Cannot update 'fieldNameInBuilder' before it is set");
    }
    this.fieldNameInBuilder =
        changedValue(fieldNameInBuilderUpdater.apply(this.fieldNameInBuilder.value()));
    return this;
  }

  /**
   * Sets the value for <code>fieldType</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldType(TypeName) setFieldType(TypeName
   * fieldType)}
   *
   * @param fieldTypeSupplier supplier for fieldType
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldType(Supplier<TypeName> fieldTypeSupplier) {
    this.fieldType = changedValue(fieldTypeSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>fieldType</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldType(TypeName) setFieldType(TypeName
   * fieldType)}
   *
   * @param fieldType fieldType
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder fieldType(TypeName fieldType) {
    this.fieldType = changedValue(fieldType);
    return this;
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementClass the list element type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeList(Class<?> elementClass) {
    return fieldType(TypeNameList.of(elementClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameList built from the given list element type.
   *
   * @param elementType the list element type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeList(TypeName elementType) {
    return fieldType(TypeNameList.of(elementType));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementClass the set element type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeSet(Class<?> elementClass) {
    return fieldType(TypeNameSet.of(elementClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameSet built from the given set element type.
   *
   * @param elementType the set element type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeSet(TypeName elementType) {
    return fieldType(TypeNameSet.of(elementType));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerClass the array component type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeArray(Class<?> innerClass) {
    return fieldType(TypeNameArray.of(innerClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameArray built from the given array component type.
   *
   * @param innerType the array component type
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeArray(TypeName innerType) {
    return fieldType(TypeNameArray.of(innerType));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameGeneric built from the given generic outer type and
   * type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeGeneric(
      Class<?> outerType, Class<?>... innerTypeArguments) {
    return fieldType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameGeneric built from the given generic outer type and
   * type arguments.
   *
   * @param outerType the generic outer type and type arguments
   * @param innerTypeArguments the generic outer type and type arguments
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeGeneric(
      TypeName outerType, TypeName... innerTypeArguments) {
    return fieldType(TypeNameGeneric.of(outerType, innerTypeArguments));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyClass the map key and value types
   * @param valueClass the map key and value types
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeMap(Class<?> keyClass, Class<?> valueClass) {
    return fieldType(TypeNameMap.of(keyClass, valueClass));
  }

  /**
   * Sets <code>fieldType</code> to a TypeNameMap built from the given map key and value types.
   *
   * @param keyType the map key and value types
   * @param valueType the map key and value types
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldTypeMap(TypeName keyType, TypeName valueType) {
    return fieldType(TypeNameMap.of(keyType, valueType));
  }

  /**
   * Sets <code>fieldType</code> by class.
   *
   * @param fieldType the class to set as <code>fieldType</code>
   * @return fluent {@code builder} instance
   */
  public BuilderFieldDtoBuilder fieldType(Class<?> fieldType) {
    return fieldType(TypeName.of(fieldType));
  }

  /**
   * Updates the current value of <code>fieldType</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link BuilderFieldDto#setFieldType(TypeName) setFieldType(TypeName
   * fieldType)}
   *
   * @param fieldTypeUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>fieldType</code> has not been set yet
   */
  public BuilderFieldDtoBuilder fieldTypeUpdate(UnaryOperator<TypeName> fieldTypeUpdater) {
    if (!this.fieldType.isSet()) {
      throw new IllegalStateException("Cannot update 'fieldType' before it is set");
    }
    this.fieldType = changedValue(fieldTypeUpdater.apply(this.fieldType.value()));
    return this;
  }

  /**
   * Sets the value for <code>getterInfo</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setGetterInfo(GetterInfoDto)
   * setGetterInfo(GetterInfoDto getterInfo)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterInfo(getterInfoDtoBuilder -> getterInfoDtoBuilder);
   * }</pre>
   *
   * @param getterInfoBuilderConsumer consumer providing an instance of a builder for getterInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder getterInfo(
      Consumer<GetterInfoDtoBuilder> getterInfoBuilderConsumer) {
    GetterInfoDtoBuilder builder =
        this.getterInfo.isSet()
            ? new GetterInfoDtoBuilder(this.getterInfo.value())
            : GetterInfoDtoBuilder.create();
    getterInfoBuilderConsumer.accept(builder);
    this.getterInfo = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>getterInfo</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setGetterInfo(GetterInfoDto)
   * setGetterInfo(GetterInfoDto getterInfo)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterInfo(() -> GetterInfoDtoBuilder.create().build());
   * }</pre>
   *
   * @param getterInfoSupplier supplier for getterInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder getterInfo(Supplier<GetterInfoDto> getterInfoSupplier) {
    this.getterInfo = changedValue(getterInfoSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>getterInfo</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setGetterInfo(GetterInfoDto)
   * setGetterInfo(GetterInfoDto getterInfo)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.getterInfo(GetterInfoDtoBuilder.create().build());
   * }</pre>
   *
   * @param getterInfo getterInfo
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder getterInfo(GetterInfoDto getterInfo) {
    this.getterInfo = changedValue(getterInfo);
    return this;
  }

  /**
   * Updates the current value of <code>getterInfo</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple values of a nested DTO, prefer the
   * builder-consumer helper {@link #getterInfo(Consumer)}.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setGetterInfo(GetterInfoDto)
   * setGetterInfo(GetterInfoDto getterInfo)}
   *
   * @param getterInfoUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>getterInfo</code> has not been set yet
   */
  public BuilderFieldDtoBuilder getterInfoUpdate(UnaryOperator<GetterInfoDto> getterInfoUpdater) {
    if (!this.getterInfo.isSet()) {
      throw new IllegalStateException("Cannot update 'getterInfo' before it is set");
    }
    this.getterInfo = changedValue(getterInfoUpdater.apply(this.getterInfo.value()));
    return this;
  }

  /**
   * Sets the value for <code>javaDoc</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setJavaDoc(JavadocDto) setJavaDoc(JavadocDto
   * javaDoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javaDoc(javadocDtoBuilder -> javadocDtoBuilder);
   * }</pre>
   *
   * @param javaDocBuilderConsumer consumer providing an instance of a builder for javaDoc
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder javaDoc(Consumer<JavadocDtoBuilder> javaDocBuilderConsumer) {
    JavadocDtoBuilder builder =
        this.javaDoc.isSet()
            ? new JavadocDtoBuilder(this.javaDoc.value())
            : JavadocDtoBuilder.create();
    javaDocBuilderConsumer.accept(builder);
    this.javaDoc = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>javaDoc</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setJavaDoc(JavadocDto) setJavaDoc(JavadocDto
   * javaDoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javaDoc(JavadocDto::new);
   * }</pre>
   *
   * @param javaDocSupplier supplier for javaDoc
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder javaDoc(Supplier<JavadocDto> javaDocSupplier) {
    this.javaDoc = changedValue(javaDocSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>javaDoc</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setJavaDoc(JavadocDto) setJavaDoc(JavadocDto
   * javaDoc)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.javaDoc(new JavadocDto());
   * }</pre>
   *
   * @param javaDoc javaDoc
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder javaDoc(JavadocDto javaDoc) {
    this.javaDoc = changedValue(javaDoc);
    return this;
  }

  /**
   * Updates the current value of <code>javaDoc</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance). For changing multiple values of a nested DTO, prefer the
   * builder-consumer helper {@link #javaDoc(Consumer)}.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setJavaDoc(JavadocDto) setJavaDoc(JavadocDto
   * javaDoc)}
   *
   * @param javaDocUpdater operator applied to the current value; its result becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>javaDoc</code> has not been set yet
   */
  public BuilderFieldDtoBuilder javaDocUpdate(UnaryOperator<JavadocDto> javaDocUpdater) {
    if (!this.javaDoc.isSet()) {
      throw new IllegalStateException("Cannot update 'javaDoc' before it is set");
    }
    this.javaDoc = changedValue(javaDocUpdater.apply(this.javaDoc.value()));
    return this;
  }

  /**
   * Sets the value for <code>nonNullable</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setNonNullable(boolean) setNonNullable(boolean
   * nonNullable)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nonNullable(() -> true);
   * }</pre>
   *
   * @param nonNullableSupplier supplier for nonNullable
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder nonNullable(Supplier<Boolean> nonNullableSupplier) {
    this.nonNullable = changedValue(nonNullableSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>nonNullable</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setNonNullable(boolean) setNonNullable(boolean
   * nonNullable)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nonNullable(true);
   * }</pre>
   *
   * @param nonNullable nonNullable
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder nonNullable(boolean nonNullable) {
    this.nonNullable = changedValue(nonNullable);
    return this;
  }

  /**
   * Updates the current value of <code>nonNullable</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link BuilderFieldDto#setNonNullable(boolean) setNonNullable(boolean
   * nonNullable)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.nonNullable(true).nonNullableUpdate(value -> !value);
   * }</pre>
   *
   * @param nonNullableUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>nonNullable</code> has not been set yet
   */
  public BuilderFieldDtoBuilder nonNullableUpdate(UnaryOperator<Boolean> nonNullableUpdater) {
    if (!this.nonNullable.isSet()) {
      throw new IllegalStateException("Cannot update 'nonNullable' before it is set");
    }
    this.nonNullable = changedValue(nonNullableUpdater.apply(this.nonNullable.value()));
    return this;
  }

  /**
   * Sets the value for <code>originalFieldName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param originalFieldNameStringBuilderConsumer consumer providing an instance of
   *     originalFieldName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalFieldName(
      Consumer<StringBuilder> originalFieldNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    originalFieldNameStringBuilderConsumer.accept(builder);
    this.originalFieldName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>originalFieldName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName(() -> "example originalFieldName");
   * }</pre>
   *
   * @param originalFieldNameSupplier supplier for originalFieldName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalFieldName(Supplier<String> originalFieldNameSupplier) {
    this.originalFieldName = changedValue(originalFieldNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>originalFieldName</code> by using String.format(format, args).
   * See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalFieldName(String format, Object... args) {
    this.originalFieldName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>originalFieldName</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName("example originalFieldName");
   * }</pre>
   *
   * @param originalFieldName originalFieldName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalFieldName(String originalFieldName) {
    this.originalFieldName = changedValue(originalFieldName);
    return this;
  }

  /**
   * Updates the current value of <code>originalFieldName</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance).
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalFieldName(String)
   * setOriginalFieldName(String originalFieldName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalFieldName("example originalFieldName").originalFieldNameUpdate(String::trim);
   * }</pre>
   *
   * @param originalFieldNameUpdater operator applied to the current value; its result becomes the
   *     new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>originalFieldName</code> has not been set yet
   */
  public BuilderFieldDtoBuilder originalFieldNameUpdate(
      UnaryOperator<String> originalFieldNameUpdater) {
    if (!this.originalFieldName.isSet()) {
      throw new IllegalStateException("Cannot update 'originalFieldName' before it is set");
    }
    this.originalFieldName =
        changedValue(originalFieldNameUpdater.apply(this.originalFieldName.value()));
    return this;
  }

  /**
   * Sets the value for <code>originalJavaDocDescription</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription(sb -> sb.append("text"));
   * }</pre>
   *
   * @param originalJavaDocDescriptionStringBuilderConsumer consumer providing an instance of
   *     originalJavaDocDescription
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalJavaDocDescription(
      Consumer<StringBuilder> originalJavaDocDescriptionStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    originalJavaDocDescriptionStringBuilderConsumer.accept(builder);
    this.originalJavaDocDescription = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>originalJavaDocDescription</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription(() -> "example originalJavaDocDescription");
   * }</pre>
   *
   * @param originalJavaDocDescriptionSupplier supplier for originalJavaDocDescription
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalJavaDocDescription(
      Supplier<String> originalJavaDocDescriptionSupplier) {
    this.originalJavaDocDescription = changedValue(originalJavaDocDescriptionSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>originalJavaDocDescription</code> by using
   * String.format(format, args). See {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalJavaDocDescription(String format, Object... args) {
    this.originalJavaDocDescription = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>originalJavaDocDescription</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription("example originalJavaDocDescription");
   * }</pre>
   *
   * @param originalJavaDocDescription originalJavaDocDescription
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder originalJavaDocDescription(String originalJavaDocDescription) {
    this.originalJavaDocDescription = changedValue(originalJavaDocDescription);
    return this;
  }

  /**
   * Updates the current value of <code>originalJavaDocDescription</code> in place by applying the
   * given operator, instead of reading it out, changing it and setting it again. Useful for
   * adjustments relative to the current value, e.g. trimming, upper-casing, clamping or
   * incrementing, and in combination with the <code>With</code> copy-and-modify flow. The value
   * must have been set before (directly or via an existing instance).
   *
   * <p>Generated from setter {@link BuilderFieldDto#setOriginalJavaDocDescription(String)
   * setOriginalJavaDocDescription(String originalJavaDocDescription)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.originalJavaDocDescription("example originalJavaDocDescription")
   *     .originalJavaDocDescriptionUpdate(String::trim);
   * }</pre>
   *
   * @param originalJavaDocDescriptionUpdater operator applied to the current value; its result
   *     becomes the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>originalJavaDocDescription</code> has not been set yet
   */
  public BuilderFieldDtoBuilder originalJavaDocDescriptionUpdate(
      UnaryOperator<String> originalJavaDocDescriptionUpdater) {
    if (!this.originalJavaDocDescription.isSet()) {
      throw new IllegalStateException(
          "Cannot update 'originalJavaDocDescription' before it is set");
    }
    this.originalJavaDocDescription =
        changedValue(
            originalJavaDocDescriptionUpdater.apply(this.originalJavaDocDescription.value()));
    return this;
  }

  /**
   * Adds a single element to <code>parameterAnnotations</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.add2ParameterAnnotations(AnnotationDtoBuilder.create().build());
   * }</pre>
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder add2ParameterAnnotations(AnnotationDto element) {
    List<AnnotationDto> newCollection;
    if (this.parameterAnnotations.isSet()) {
      newCollection = new ArrayList<>(this.parameterAnnotations.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.parameterAnnotations = changedValue(newCollection);
    return this;
  }

  /**
   * Sets the value for <code>parameterAnnotations</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterAnnotations(AnnotationDtoBuilder.create().build(), AnnotationDtoBuilder.create().build());
   * }</pre>
   *
   * @param parameterAnnotations parameterAnnotations
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder parameterAnnotations(AnnotationDto... parameterAnnotations) {
    this.parameterAnnotations = changedValue(List.of(parameterAnnotations));
    return this;
  }

  /**
   * Sets the value for <code>parameterAnnotations</code> using a builder consumer that produces the
   * value.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterAnnotations(t -> t.add(annotationDtoBuilder -> annotationDtoBuilder));
   * }</pre>
   *
   * @param parameterAnnotationsBuilderConsumer consumer providing an instance of a builder for
   *     parameterAnnotations
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder parameterAnnotations(
      Consumer<ArrayListBuilderWithElementBuilders<AnnotationDto, AnnotationDtoElementBuilder>>
          parameterAnnotationsBuilderConsumer) {
    ArrayListBuilderWithElementBuilders<AnnotationDto, AnnotationDtoElementBuilder> builder =
        this.parameterAnnotations.isSet()
            ? new ArrayListBuilderWithElementBuilders<AnnotationDto, AnnotationDtoElementBuilder>(
                this.parameterAnnotations.value(), AnnotationDtoElementBuilder::new)
            : new ArrayListBuilderWithElementBuilders<AnnotationDto, AnnotationDtoElementBuilder>(
                AnnotationDtoElementBuilder::new);
    parameterAnnotationsBuilderConsumer.accept(builder);
    this.parameterAnnotations = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>parameterAnnotations</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterAnnotations(() -> List.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param parameterAnnotationsSupplier supplier for parameterAnnotations
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder parameterAnnotations(
      Supplier<List<AnnotationDto>> parameterAnnotationsSupplier) {
    this.parameterAnnotations = changedValue(parameterAnnotationsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>parameterAnnotations</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterAnnotations(List.of(AnnotationDtoBuilder.create().build()));
   * }</pre>
   *
   * @param parameterAnnotations parameterAnnotations
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder parameterAnnotations(List<AnnotationDto> parameterAnnotations) {
    this.parameterAnnotations = changedValue(parameterAnnotations);
    return this;
  }

  /**
   * Updates the current value of <code>parameterAnnotations</code> in place by applying the given
   * operator, instead of reading it out, changing it and setting it again. Useful for adjustments
   * relative to the current value, e.g. trimming, upper-casing, clamping or incrementing, and in
   * combination with the <code>With</code> copy-and-modify flow. The value must have been set
   * before (directly or via an existing instance). For changing multiple elements of a nested DTO,
   * prefer the builder-consumer helper {@link #parameterAnnotations(Consumer)}.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setParameterAnnotations(List)
   * setParameterAnnotations(List<AnnotationDto> parameterAnnotations)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.parameterAnnotations(List.of(AnnotationDtoBuilder.create().build()))
   *     .parameterAnnotationsUpdate(list -> list.stream().sorted().toList());
   * }</pre>
   *
   * @param parameterAnnotationsUpdater operator applied to the current value; its result becomes
   *     the new value
   * @return current instance of builder
   * @throws IllegalStateException if <code>parameterAnnotations</code> has not been set yet
   */
  public BuilderFieldDtoBuilder parameterAnnotationsUpdate(
      UnaryOperator<List<AnnotationDto>> parameterAnnotationsUpdater) {
    if (!this.parameterAnnotations.isSet()) {
      throw new IllegalStateException("Cannot update 'parameterAnnotations' before it is set");
    }
    this.parameterAnnotations =
        changedValue(parameterAnnotationsUpdater.apply(this.parameterAnnotations.value()));
    return this;
  }

  /**
   * Sets the value for <code>setterName</code> by executing the provided consumer.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName(sb -> sb.append("text"));
   * }</pre>
   *
   * @param setterNameStringBuilderConsumer consumer providing an instance of setterName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder setterName(
      Consumer<StringBuilder> setterNameStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    setterNameStringBuilderConsumer.accept(builder);
    this.setterName = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>setterName</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName(() -> "example setterName");
   * }</pre>
   *
   * @param setterNameSupplier supplier for setterName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder setterName(Supplier<String> setterNameSupplier) {
    this.setterName = changedValue(setterNameSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>setterName</code> by using String.format(format, args). See
   * {@link String#format(String, Object...)} for details.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder setterName(String format, Object... args) {
    this.setterName = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>setterName</code>.
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName("example setterName");
   * }</pre>
   *
   * @param setterName setterName
   * @return current instance of builder
   */
  public BuilderFieldDtoBuilder setterName(String setterName) {
    this.setterName = changedValue(setterName);
    return this;
  }

  /**
   * Updates the current value of <code>setterName</code> in place by applying the given operator,
   * instead of reading it out, changing it and setting it again. Useful for adjustments relative to
   * the current value, e.g. trimming, upper-casing, clamping or incrementing, and in combination
   * with the <code>With</code> copy-and-modify flow. The value must have been set before (directly
   * or via an existing instance).
   *
   * <p>Generated from setter {@link BuilderFieldDto#setSetterName(String) setSetterName(String
   * setterName)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.setterName("example setterName").setterNameUpdate(String::trim);
   * }</pre>
   *
   * @param setterNameUpdater operator applied to the current value; its result becomes the new
   *     value
   * @return current instance of builder
   * @throws IllegalStateException if <code>setterName</code> has not been set yet
   */
  public BuilderFieldDtoBuilder setterNameUpdate(UnaryOperator<String> setterNameUpdater) {
    if (!this.setterName.isSet()) {
      throw new IllegalStateException("Cannot update 'setterName' before it is set");
    }
    this.setterName = changedValue(setterNameUpdater.apply(this.setterName.value()));
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public BuilderFieldDtoBuilder conditional(
      BooleanSupplier condition, Consumer<BuilderFieldDtoBuilder> yesCondition) {
    return conditional(condition, yesCondition, null);
  }

  /**
   * Conditionally applies builder modifications based on a condition evaluation.
   *
   * @param condition the condition to evaluate
   * @param trueCase the consumer to apply if condition is true
   * @param falseCase the consumer to apply if condition is false (can be null)
   * @return this builder instance
   */
  public BuilderFieldDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<BuilderFieldDtoBuilder> trueCase,
      Consumer<BuilderFieldDtoBuilder> falseCase) {
    if (condition.getAsBoolean()) {
      trueCase.accept(this);
    } else if (falseCase != null) {
      falseCase.accept(this);
    }
    return this;
  }

  /**
   * Builds the configured DTO instance.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * BuilderFieldDto result = builder.build();
   * }</pre>
   */
  @Override
  public BuilderFieldDto build() {
    if (this.nonNullable.isSet() && this.nonNullable.value() == null) {
      throw new IllegalStateException(
          "Field 'nonNullable' is marked as non-null but null value was provided");
    }
    BuilderFieldDto result = new BuilderFieldDto();
    this.defaultValue.ifSet(result::setDefaultValue);
    this.deprecationInfo.ifSet(result::setDeprecationInfo);
    this.fieldGenerics.ifSet(result::setFieldGenerics);
    this.fieldNameInBuilder.ifSet(result::setFieldNameInBuilder);
    this.fieldType.ifSet(result::setFieldType);
    this.getterInfo.ifSet(result::setGetterInfo);
    this.javaDoc.ifSet(result::setJavaDoc);
    this.nonNullable.ifSet(result::setNonNullable);
    this.originalFieldName.ifSet(result::setOriginalFieldName);
    this.originalJavaDocDescription.ifSet(result::setOriginalJavaDocDescription);
    this.parameterAnnotations.ifSet(result::setParameterAnnotations);
    this.setterName.ifSet(result::setSetterName);
    return result;
  }

  /**
   * Returns a string representation of this builder, including only fields that have been set.
   *
   * @return string representation of the builder
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, BuilderToStringStyle.INSTANCE)
        .append("defaultValue", this.defaultValue)
        .append("deprecationInfo", this.deprecationInfo)
        .append("fieldGenerics", this.fieldGenerics)
        .append("fieldNameInBuilder", this.fieldNameInBuilder)
        .append("fieldType", this.fieldType)
        .append("getterInfo", this.getterInfo)
        .append("javaDoc", this.javaDoc)
        .append("nonNullable", this.nonNullable)
        .append("originalFieldName", this.originalFieldName)
        .append("originalJavaDocDescription", this.originalJavaDocDescription)
        .append("parameterAnnotations", this.parameterAnnotations)
        .append("setterName", this.setterName)
        .toString();
  }

  /** Interface that can be implemented by the DTO to provide fluent modification methods. */
  public interface With {
    /**
     * Initializes a builder from an instance of this class, using methods of this builder to change
     * values and returns the new built object.
     *
     * @param b the consumer to apply modifications
     * @return the modified instance
     */
    default BuilderFieldDto with(Consumer<BuilderFieldDtoBuilder> b) {
      BuilderFieldDtoBuilder builder;
      try {
        builder = new BuilderFieldDtoBuilder(BuilderFieldDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'BuilderFieldDtoBuilder.With' should only be implemented by classes, which could be casted to 'BuilderFieldDto'",
            ex);
      }
      b.accept(builder);
      return builder.build();
    }

    /**
     * Creates a builder initialized from this instance.
     *
     * @return a builder initialized with this instance's values
     */
    default BuilderFieldDtoBuilder with() {
      try {
        return new BuilderFieldDtoBuilder(BuilderFieldDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'BuilderFieldDtoBuilder.With' should only be implemented by classes, which could be casted to 'BuilderFieldDto'",
            ex);
      }
    }
  }

  /**
   * Element builder for {@link GenericParameterDto} collections that bridges the codegen builder to
   * the core {@link IBuilderBase} contract used by the core collection helpers.
   */
  public static final class GenericParameterDtoElementBuilder extends GenericParameterDtoBuilder
      implements IBuilderBase<GenericParameterDto> {}

  /**
   * Element builder for {@link AnnotationDto} collections that bridges the codegen builder to the
   * core {@link IBuilderBase} contract used by the core collection helpers.
   */
  public static final class AnnotationDtoElementBuilder extends AnnotationDtoBuilder
      implements IBuilderBase<AnnotationDto> {}
}
