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

/**
 * Implementation of an {@link Optional} not containing a reference.
 */
final class Absent<T> extends Optional<T> {
  static final Absent<Object> INSTANCE = new Absent<Object>();

  @SuppressWarnings("unchecked") // implementation is "fully variant"
  static <T> Optional<T> withType() {
    return (Optional<T>) INSTANCE;
  }

  private Absent() {}

  @Override public boolean isPresent() {
    return false;
  }

  @Override public T get() {
    throw new IllegalStateException("Optional.get() cannot be called on an empty value");
  }

  @Override public T orElse(T defaultValue) {
    return defaultValue;
  }

  @Override public boolean equals(Object object) {
    return object == this;
  }

  @Override public int hashCode() {
    return 0x598df91c;
  }

  @Override public String toString() {
    return "Optional.empty()";
  }

  private Object readResolve() {
    return INSTANCE;
  }

  private static final long serialVersionUID = 0;
}
