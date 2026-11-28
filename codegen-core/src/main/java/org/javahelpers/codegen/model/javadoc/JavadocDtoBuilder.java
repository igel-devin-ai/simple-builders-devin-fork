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

package org.javahelpers.codegen.model.javadoc;

import static org.javahelpers.codegen.builders.TrackedValue.changedValue;
import static org.javahelpers.codegen.builders.TrackedValue.initialValue;
import static org.javahelpers.codegen.builders.TrackedValue.unsetValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javahelpers.codegen.builders.ArrayListBuilder;
import org.javahelpers.codegen.builders.BuilderToStringStyle;
import org.javahelpers.codegen.builders.IBuilderBase;
import org.javahelpers.codegen.builders.TrackedValue;

/**
 * Builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto}.
 *
 * <p>This builder provides a fluent API for creating instances of
 * org.javahelpers.codegen.model.javadoc.JavadocDto with method chaining and validation. Use the
 * static {@code create()} method to obtain a new builder instance, configure the desired properties
 * using the setter methods, and then call {@code build()} to create the final DTO.
 *
 * <h4>Example:</h4>
 *
 * <pre>{@code
 * JavadocDto result = JavadocDtoBuilder.create()
 *     .format("example format")
 *     .format("Hello %s", "World")
 *     .format(() -> "example format")
 *     .format(sb -> sb.append("text"))
 *     .exampleUsageCodeBlock(new JavadocCodeBlockDto())
 *     .exampleUsageCodeBlock(JavadocCodeBlockDto::new)
 *     .exampleUsageCodeBlock(javadocCodeBlockDtoBuilder -> javadocCodeBlockDtoBuilder)
 *     .build();
 * }</pre>
 */
public class JavadocDtoBuilder implements IBuilderBase<JavadocDto> {

  /** Tracked value for <code>format</code>: format. */
  private TrackedValue<String> format = unsetValue();

  /** Tracked value for <code>args</code>: args. */
  private TrackedValue<Object[]> args = unsetValue();

  /**
   * Tracked value for <code>exampleUsageCodeBlock</code>: the usage-example code block, or {@code
   * null} to clear it.
   */
  private TrackedValue<JavadocCodeBlockDto> exampleUsageCodeBlock = unsetValue();

  /** Tracked value for <code>tags</code>: the replacement collection (not null). */
  private TrackedValue<List<JavadocTagDto>> tags = unsetValue();

  /** Empty constructor of builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto}. */
  public JavadocDtoBuilder() {}

  /**
   * Initialisation of builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto} by a
   * instance.
   *
   * @param instance object instance for initialisiation
   */
  public JavadocDtoBuilder(JavadocDto instance) {
    if (instance.getDescription() != null) {
      this.format = initialValue(instance.getDescription().replace("%", "%%"));
    }
    this.args = initialValue(new Object[0]);
    this.exampleUsageCodeBlock = initialValue(instance.getExampleUsageCodeBlock());
    this.tags = initialValue(instance.getTags());
  }

  /**
   * Creating a new builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto}.
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * JavadocDtoBuilder builder = JavadocDtoBuilder.create();
   * }</pre>
   *
   * @return builder for {@code org.javahelpers.codegen.model.javadoc.JavadocDto}
   */
  public static JavadocDtoBuilder create() {
    return new JavadocDtoBuilder();
  }

  /**
   * Sets the value for <code>args</code>.
   *
   * <p>Generated from parameter in constructor {@link JavadocDto#JavadocDto(String, Object)
   * JavadocDto(String format, Object args)}
   *
   * @param args args
   * @return current instance of builder
   */
  public JavadocDtoBuilder args(List<Object> args) {
    this.args = changedValue(args.toArray(new Object[0]));
    return this;
  }

  /**
   * Sets the value for <code>args</code> using the fluent builder consumer.
   *
   * <p>Generated from parameter in constructor {@link JavadocDto#JavadocDto(String, Object)
   * JavadocDto(String format, Object args)}
   *
   * @param argsBuilderConsumer consumer for args
   * @return current instance of builder
   */
  public JavadocDtoBuilder args(Consumer<ArrayListBuilder<Object>> argsBuilderConsumer) {
    ArrayListBuilder<Object> builder;
    if (this.args.isSet()) {
      builder = new ArrayListBuilder<Object>(Arrays.asList(this.args.value()));
    } else {
      builder = new ArrayListBuilder<Object>(Arrays.asList());
    }
    argsBuilderConsumer.accept(builder);
    this.args = changedValue(builder.build().toArray(new Object[0]));
    return this;
  }

  /**
   * Sets the value for <code>args</code> by invoking the provided supplier.
   *
   * <p>Generated from parameter in constructor {@link JavadocDto#JavadocDto(String, Object)
   * JavadocDto(String format, Object args)}
   *
   * @param argsSupplier supplier for args
   * @return current instance of builder
   */
  public JavadocDtoBuilder args(Supplier<Object[]> argsSupplier) {
    this.args = changedValue(argsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>args</code>.
   *
   * <p>Generated from parameter in constructor {@link JavadocDto#JavadocDto(String, Object)
   * JavadocDto(String format, Object args)}
   *
   * @param args args
   * @return current instance of builder
   */
  public JavadocDtoBuilder args(Object... args) {
    this.args = changedValue(args);
    return this;
  }

  /**
   * Sets the value for <code>exampleUsageCodeBlock</code> using a builder consumer that produces
   * the value.
   *
   * <p>Generated from setter {@link JavadocDto#setExampleUsageCodeBlock(JavadocCodeBlockDto)
   * setExampleUsageCodeBlock(JavadocCodeBlockDto exampleUsageCodeBlock)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleUsageCodeBlock(javadocCodeBlockDtoBuilder -> javadocCodeBlockDtoBuilder);
   * }</pre>
   *
   * @param exampleUsageCodeBlockBuilderConsumer consumer providing an instance of a builder for the
   *     usage-example code block, or {@code null} to clear it
   * @return current instance of builder
   */
  public JavadocDtoBuilder exampleUsageCodeBlock(
      Consumer<JavadocCodeBlockDtoBuilder> exampleUsageCodeBlockBuilderConsumer) {
    JavadocCodeBlockDtoBuilder builder =
        this.exampleUsageCodeBlock.isSet()
            ? new JavadocCodeBlockDtoBuilder(this.exampleUsageCodeBlock.value())
            : JavadocCodeBlockDtoBuilder.create();
    exampleUsageCodeBlockBuilderConsumer.accept(builder);
    this.exampleUsageCodeBlock = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>exampleUsageCodeBlock</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link JavadocDto#setExampleUsageCodeBlock(JavadocCodeBlockDto)
   * setExampleUsageCodeBlock(JavadocCodeBlockDto exampleUsageCodeBlock)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleUsageCodeBlock(JavadocCodeBlockDto::new);
   * }</pre>
   *
   * @param exampleUsageCodeBlockSupplier supplier for the usage-example code block, or {@code null}
   *     to clear it
   * @return current instance of builder
   */
  public JavadocDtoBuilder exampleUsageCodeBlock(
      Supplier<JavadocCodeBlockDto> exampleUsageCodeBlockSupplier) {
    this.exampleUsageCodeBlock = changedValue(exampleUsageCodeBlockSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>exampleUsageCodeBlock</code>.
   *
   * <p>Generated from setter {@link JavadocDto#setExampleUsageCodeBlock(JavadocCodeBlockDto)
   * setExampleUsageCodeBlock(JavadocCodeBlockDto exampleUsageCodeBlock)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.exampleUsageCodeBlock(new JavadocCodeBlockDto());
   * }</pre>
   *
   * @param exampleUsageCodeBlock the usage-example code block, or {@code null} to clear it
   * @return current instance of builder
   */
  public JavadocDtoBuilder exampleUsageCodeBlock(JavadocCodeBlockDto exampleUsageCodeBlock) {
    this.exampleUsageCodeBlock = changedValue(exampleUsageCodeBlock);
    return this;
  }

  /**
   * Sets the value for <code>format</code> by executing the provided consumer.
   *
   * <p>The text is passed through {@link String#format(String, Object...)} at {@link #build()}.
   * Write a literal percent sign as {@code %%}, e.g. {@code format("100%%").args()} builds {@code
   * "100%"}.
   *
   * <p>Generated from parameter in constructor {@link JavadocDto#JavadocDto(String, Object)
   * JavadocDto(String format, Object args)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.format(sb -> sb.append("text"));
   * }</pre>
   *
   * @param formatStringBuilderConsumer consumer providing an instance of format
   * @return current instance of builder
   */
  public JavadocDtoBuilder format(Consumer<StringBuilder> formatStringBuilderConsumer) {
    StringBuilder builder = new StringBuilder();
    formatStringBuilderConsumer.accept(builder);
    this.format = changedValue(builder.toString());
    return this;
  }

  /**
   * Sets the value for <code>format</code> by invoking the provided supplier.
   *
   * <p>The text is passed through {@link String#format(String, Object...)} at {@link #build()}.
   * Write a literal percent sign as {@code %%}, e.g. {@code format("100%%").args()} builds {@code
   * "100%"}.
   *
   * <p>Generated from parameter in constructor {@link JavadocDto#JavadocDto(String, Object)
   * JavadocDto(String format, Object args)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.format(() -> "example format");
   * }</pre>
   *
   * @param formatSupplier supplier for format
   * @return current instance of builder
   */
  public JavadocDtoBuilder format(Supplier<String> formatSupplier) {
    this.format = changedValue(formatSupplier.get());
    return this;
  }

  /**
   * Sets the String value for <code>format</code> by using String.format(format, args). See {@link
   * String#format(String, Object...)} for details.
   *
   * <p>The resulting text is passed through {@link String#format(String, Object...)} again at
   * {@link #build()}. Escape literal percent signs for both formatting passes, e.g. {@code
   * format("%s %d%%%%", "Rate", 100).args()} builds {@code "Rate 100%"}.
   *
   * <p>Generated from parameter in constructor {@link JavadocDto#JavadocDto(String, Object)
   * JavadocDto(String format, Object args)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.format("Hello %s", "World");
   * }</pre>
   *
   * @param format A format string
   * @param args Arguments referenced by the format specifiers in the format string.
   * @return current instance of builder
   */
  public JavadocDtoBuilder format(String format, Object... args) {
    this.format = changedValue(String.format(format, args));
    return this;
  }

  /**
   * Sets the value for <code>format</code>.
   *
   * <p>The text is passed through {@link String#format(String, Object...)} at {@link #build()}.
   * Write a literal percent sign as {@code %%}, e.g. {@code format("100%%").args()} builds {@code
   * "100%"}.
   *
   * <p>Generated from parameter in constructor {@link JavadocDto#JavadocDto(String, Object)
   * JavadocDto(String format, Object args)}
   *
   * <h4>Example:</h4>
   *
   * <pre>{@code
   * builder.format("example format");
   * }</pre>
   *
   * @param format format
   * @return current instance of builder
   */
  public JavadocDtoBuilder format(String format) {
    this.format = changedValue(format);
    return this;
  }

  /**
   * Adds a single element to <code>tags</code>.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param element the element to add
   * @return current instance of builder
   */
  public JavadocDtoBuilder add2Tags(JavadocTagDto element) {
    List<JavadocTagDto> newCollection;
    if (this.tags.isSet()) {
      newCollection = new ArrayList<>(this.tags.value());
    } else {
      newCollection = new ArrayList<>();
    }
    newCollection.add(element);
    this.tags = changedValue(newCollection);
    return this;
  }

  /**
   * Sets the value for <code>tags</code>.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param tags the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder tags(JavadocTagDto... tags) {
    this.tags = changedValue(List.of(tags));
    return this;
  }

  /**
   * Sets the value for <code>tags</code> using a builder consumer that produces the value.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param tagsBuilderConsumer consumer providing an instance of a builder for the replacement
   *     collection (not null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder tags(Consumer<ArrayListBuilder<JavadocTagDto>> tagsBuilderConsumer) {
    ArrayListBuilder<JavadocTagDto> builder =
        this.tags.isSet()
            ? new ArrayListBuilder<JavadocTagDto>(this.tags.value())
            : new ArrayListBuilder<JavadocTagDto>();
    tagsBuilderConsumer.accept(builder);
    this.tags = changedValue(builder.build());
    return this;
  }

  /**
   * Sets the value for <code>tags</code> by invoking the provided supplier.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param tagsSupplier supplier for the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder tags(Supplier<List<JavadocTagDto>> tagsSupplier) {
    this.tags = changedValue(tagsSupplier.get());
    return this;
  }

  /**
   * Sets the value for <code>tags</code>.
   *
   * <p>Generated from setter {@link JavadocDto#setTags(List) setTags(List<JavadocTagDto> tags)}
   *
   * @param tags the replacement collection (not null)
   * @return current instance of builder
   */
  public JavadocDtoBuilder tags(List<JavadocTagDto> tags) {
    this.tags = changedValue(tags);
    return this;
  }

  /**
   * Conditionally applies builder modifications if the condition is true.
   *
   * @param condition the condition to evaluate
   * @param yesCondition the consumer to apply if condition is true
   * @return this builder instance
   */
  public JavadocDtoBuilder conditional(
      BooleanSupplier condition, Consumer<JavadocDtoBuilder> yesCondition) {
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
  public JavadocDtoBuilder conditional(
      BooleanSupplier condition,
      Consumer<JavadocDtoBuilder> trueCase,
      Consumer<JavadocDtoBuilder> falseCase) {
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
   * JavadocDto result = builder.build();
   * }</pre>
   */
  @Override
  public JavadocDto build() {
    JavadocDto result = new JavadocDto(this.format.value(), this.args.value());
    this.exampleUsageCodeBlock.ifSet(result::setExampleUsageCodeBlock);
    this.tags.ifSet(result::setTags);
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
        .append("format", this.format)
        .append("args", this.args)
        .append("exampleUsageCodeBlock", this.exampleUsageCodeBlock)
        .append("tags", this.tags)
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
    default JavadocDto with(Consumer<JavadocDtoBuilder> b) {
      JavadocDtoBuilder builder;
      try {
        builder = new JavadocDtoBuilder(JavadocDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'JavadocDtoBuilder.With' should only be implemented by classes, which could be casted to 'JavadocDto'",
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
    default JavadocDtoBuilder with() {
      try {
        return new JavadocDtoBuilder(JavadocDto.class.cast(this));
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException(
            "The interface 'JavadocDtoBuilder.With' should only be implemented by classes, which could be casted to 'JavadocDto'",
            ex);
      }
    }
  }
}
