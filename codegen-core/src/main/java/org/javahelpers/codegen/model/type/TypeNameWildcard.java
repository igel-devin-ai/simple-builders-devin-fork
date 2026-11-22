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

package org.javahelpers.codegen.model.type;

import java.util.Objects;
import java.util.Optional;

/** A wildcard type argument, optionally bounded above or below by another type. */
public final class TypeNameWildcard extends TypeName {
  public enum BoundKind {
    UNBOUNDED,
    EXTENDS,
    SUPER
  }

  private final BoundKind boundKind;
  private final TypeName bound;

  /** Creates an unbounded wildcard. */
  public TypeNameWildcard() {
    super("", "?");
    boundKind = BoundKind.UNBOUNDED;
    bound = null;
  }

  private TypeNameWildcard(BoundKind boundKind, TypeName bound) {
    super("", "?");
    this.boundKind = boundKind;
    this.bound = Objects.requireNonNull(bound, "bound");
  }

  /**
   * Creates an upper-bounded wildcard.
   *
   * @param bound the non-null upper bound
   * @return the wildcard
   */
  public static TypeNameWildcard extendsBound(TypeName bound) {
    return new TypeNameWildcard(BoundKind.EXTENDS, bound);
  }

  /**
   * Creates a lower-bounded wildcard.
   *
   * @param bound the non-null lower bound
   * @return the wildcard
   */
  public static TypeNameWildcard superBound(TypeName bound) {
    return new TypeNameWildcard(BoundKind.SUPER, bound);
  }

  public BoundKind getBoundKind() {
    return boundKind;
  }

  public Optional<TypeName> getBound() {
    return Optional.ofNullable(bound);
  }

  @Override
  public String getFullQualifiedName() {
    return switch (boundKind) {
      case UNBOUNDED -> "?";
      case EXTENDS -> "? extends " + bound.getFullQualifiedName();
      case SUPER -> "? super " + bound.getFullQualifiedName();
    };
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TypeNameWildcard wildcard
        && super.equals(other)
        && boundKind == wildcard.boundKind
        && Objects.equals(bound, wildcard.bound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), boundKind, bound);
  }
}
