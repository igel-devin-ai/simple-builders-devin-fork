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

package org.javahelpers.simple.builders.processor.exceptions;

import javax.lang.model.element.Element;

/** Special exception for errors in processing of annotations. */
public class BuilderException extends Exception {
  private final transient Element element;
  private final boolean alwaysError;

  /**
   * Creating an exception with location element, message and parameters.
   *
   * @param element {@code javax.lang.model.element} the element on which the processing led to an
   *     error
   * @param cause root cause of current exception, containing stacktrace
   */
  public BuilderException(Element element, Throwable cause) {
    super(cause.getMessage(), cause);
    this.element = element;
    this.alwaysError = false;
  }

  /**
   * Creating an exception with location element, message and parameters.
   *
   * @param element {@code javax.lang.model.element} the element on which the processing led to an
   *     error
   * @param message A specific message, supports String.format arguments
   * @param args Arguments for Stringlformat on message
   */
  public BuilderException(Element element, String message, Object... args) {
    this(element, false, message, args);
  }

  /**
   * Creates a processing failure with an explicit severity policy.
   *
   * @param element the diagnostic location
   * @param alwaysError whether this failure is an error even outside strict mode
   * @param message the diagnostic format string
   * @param args the format arguments
   */
  public BuilderException(Element element, boolean alwaysError, String message, Object... args) {
    super(String.format(message, args));
    this.element = element;
    this.alwaysError = alwaysError;
  }

  /**
   * @return whether the failure must be reported as an error regardless of strict mode
   */
  public boolean isAlwaysError() {
    return alwaysError;
  }

  /**
   * Returns the element where processing
   *
   * @return {@code javax.lang.model.element} the element on which the processing led to an error
   */
  public Element getElement() {
    return element;
  }
}
