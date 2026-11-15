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

package org.javahelpers.simple.builders.processor.model.type;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.codegen.model.type.TypeInstantiation;
import org.javahelpers.codegen.model.type.TypeName;

/**
 * Instantiation by prefilling the generated type through its field functions: a generated static
 * {@code methodName} on the target class obtains an empty instance via {@code funcForEmptyInstance}
 * and calls one field function per prefilled property of the source value. {@link
 * #instantiationCode} renders the call to that generated method.
 *
 * @param generatedType the resolved generated type (return type of the prefilling method)
 * @param sourceType the referenced type the instance is prefilled from
 * @param funcForEmptyInstance how the prefilling method obtains the empty instance
 * @param methodPrefix the field-function prefix recognized on the generated type ({@code ""} for
 *     {@code name(v)}, {@code set} for {@code setName(v)}, or a detected convention like {@code
 *     with})
 * @param prefilledFields the properties to prefill, in the order of the source's properties
 * @param methodName the name of the prefilling method generated on the target type
 */
public record PrefillCall(
    TypeName generatedType,
    TypeName sourceType,
    TypeInstantiation funcForEmptyInstance,
    String methodPrefix,
    List<Field> prefilledFields,
    String methodName)
    implements TypeInstantiation {

  @Override
  public String instantiationCode(String typeExpression, String arguments) {
    return methodName() + "(" + arguments + ")";
  }

  @Override
  public String methodReference(String typeExpression) {
    return "this::" + methodName();
  }

  /**
   * The generated type's field function for a prefilled field: {@code <property>} when the prefix
   * is empty, otherwise {@code <prefix><Property>} (e.g. {@code setName} or {@code withName}).
   *
   * @param field the prefilled field
   * @return the field function's name on the generated type
   */
  public String fieldMethodFor(Field field) {
    return methodPrefix.isEmpty()
        ? field.property()
        : methodPrefix + StringUtils.capitalize(field.property());
  }

  /**
   * One property prefilled inside the prefilling method: {@code
   * target.<fieldFunction>(value.<accessor>)}.
   *
   * @param property the property name (e.g. {@code name})
   * @param accessor how the property is read from the value (e.g. {@code name()} or {@code name})
   */
  public record Field(String property, String accessor) {}
}
