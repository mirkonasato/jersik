/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.encoded.jersik.runtime.types;

import java.io.Serializable;

/**
 * An immutable object that may contain a non-null reference to another object. Each
 * instance of this type either contains a non-null reference, or contains nothing (in
 * which case we say that the reference is "absent"); it is never said to "contain {@code
 * null}".
 *
 * <p>A non-null {@code Optional<T>} reference can be used as a replacement for a nullable
 * {@code T} reference. It allows you to represent "a {@code T} that must be present" and
 * a "a {@code T} that might be absent" as two distinct types in your program, which can
 * aid clarity.
 *
 * <p>Some uses of this class include
 *
 * <ul>
 * <li>As a method return type, as an alternative to returning {@code null} to indicate
 *     that no value was available
 * <li>To distinguish between "unknown" (for example, not present in a map) and "known to
 *     have no value" (present in the map, with value {@code Optional.empty()})
 * <li>To wrap nullable references for storage in a collection that does not support
 *     {@code null} (though there are
 *     <a href="http://code.google.com/p/guava-libraries/wiki/LivingWithNullHostileCollections">
 *     several other approaches to this</a> that should be considered first)
 * </ul>
 *
 * <p>A common alternative to using this class is to find or create a suitable
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">null object</a> for the
 * type in question.
 *
 * <p>This class is not intended as a direct analogue of any existing "option" or "maybe"
 * construct from other programming environments, though it may bear some similarities.
 *
 * <p>See the Guava User Guide article on <a
 * href="http://code.google.com/p/guava-libraries/wiki/UsingAndAvoidingNullExplained#Optional">
 * using {@code Optional}</a>.
 *
 * <p>Note: this is a <b>modified version</b> of the
 * <a href="http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Optional.html">com.google.common.base.Optional</a>
 * class found in Guava 16.0.1. Dependencies on the rest of Guava libraries have been removed and some methods have
 * been renamed to match the API of
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html">java.util.Optional</a>
 * introduced in Java 1.8.
 *
 * @param <T> the type of instance that can be contained. {@code Optional} is naturally
 *     covariant on this type, so it is safe to cast an {@code Optional<T>} to {@code
 *     Optional<S>} for any supertype {@code S} of {@code T}.
 * @author Kurt Alfred Kluever
 * @author Kevin Bourrillion
 * @author Mirko Nasato: adapted and modified for inclusion in Wicked RPC
 */
public abstract class Optional<T> implements Serializable {
  /**
   * Returns an {@code Optional} instance with no contained reference.
   */
  public static <T> Optional<T> empty() {
    return Absent.withType();
  }

  /**
   * Returns an {@code Optional} instance containing the given non-null reference.
   */
  public static <T> Optional<T> of(T reference) {
    if (reference == null) throw new NullPointerException();
    return new Present<T>(reference);
  }

  /**
   * If {@code nullableReference} is non-null, returns an {@code Optional} instance containing that
   * reference; otherwise returns {@link Optional#empty}.
   */
  public static <T> Optional<T> ofNullable(T nullableReference) {
    return (nullableReference == null)
        ? Optional.<T>empty()
        : new Present<T>(nullableReference);
  }

  Optional() {}

  /**
   * Returns {@code true} if this holder contains a (non-null) instance.
   */
  public abstract boolean isPresent();

  /**
   * Returns the contained instance, which must be present. If the instance might be
   * absent, use {@link #or(Object)} instead.
   *
   * @throws IllegalStateException if the instance is absent ({@link #isPresent} returns
   *     {@code false})
   */
  public abstract T get();

  /**
   * Returns the contained instance if it is present; {@code defaultValue} otherwise. If
   * no default value should be required because the instance is known to be present, use
   * {@link #get()} instead.
   * 
   * @param defaultValue may be null
   */
  public abstract T orElse(T defaultValue);

  /**
   * Returns {@code true} if {@code object} is an {@code Optional} instance, and either
   * the contained references are {@linkplain Object#equals equal} to each other or both
   * are absent. Note that {@code Optional} instances of differing parameterized types can
   * be equal.
   */
  @Override
  public abstract boolean equals(Object object);

  /**
   * Returns a hash code for this instance.
   */
  @Override
  public abstract int hashCode();

  /**
   * Returns a string representation for this instance. The form of this string
   * representation is unspecified.
   */
  @Override
  public abstract String toString();

  private static final long serialVersionUID = 0;
}
