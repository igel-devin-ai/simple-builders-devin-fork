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

/**
 * Constants for the common Javadoc block tags and frequently used tag values.
 *
 * <p>The set of Javadoc tags is not closed — the javadoc tool supports custom tags — so these
 * constants cover only the well-known standard tags. Arbitrary tags can always be used via {@link
 * JavadocDto#addTag(String, String)}.
 */
public final class JavadocConstants {

  /** The {@code @param} tag. */
  public static final String PARAM_TAG = "param";

  /** The {@code @return} tag. */
  public static final String RETURN_TAG = "return";

  /** The {@code @throws} tag. */
  public static final String THROWS_TAG = "throws";

  /** The {@code @exception} tag, synonym of {@code @throws}. */
  public static final String EXCEPTION_TAG = "exception";

  /** The {@code @deprecated} tag. */
  public static final String DEPRECATED_TAG = "deprecated";

  /** The {@code @since} tag. */
  public static final String SINCE_TAG = "since";

  /** The {@code @see} tag. */
  public static final String SEE_TAG = "see";

  /** The {@code @author} tag. */
  public static final String AUTHOR_TAG = "author";

  /** The {@code @version} tag. */
  public static final String VERSION_TAG = "version";

  /** The {@code @serial} tag. */
  public static final String SERIAL_TAG = "serial";

  /** The {@code @serialField} tag. */
  public static final String SERIAL_FIELD_TAG = "serialField";

  /** The {@code @serialData} tag. */
  public static final String SERIAL_DATA_TAG = "serialData";

  /** The {@code @hidden} tag. */
  public static final String HIDDEN_TAG = "hidden";

  /** The {@code @uses} tag. */
  public static final String USES_TAG = "uses";

  /** The {@code @provides} tag. */
  public static final String PROVIDES_TAG = "provides";

  /** The {@code @value} tag. */
  public static final String VALUE_TAG = "value";

  /** Common return description for builder methods that return the builder instance. */
  public static final String RETURN_BUILDER_INSTANCE = "current instance of builder";

  /** Private constructor to prevent instantiation. */
  private JavadocConstants() {
    throw new UnsupportedOperationException("Utility class");
  }
}
