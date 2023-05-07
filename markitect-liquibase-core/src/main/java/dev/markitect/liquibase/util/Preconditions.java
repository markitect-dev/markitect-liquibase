/*
 * Copyright 2023 Markitect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.markitect.liquibase.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Preconditions {
  @CanIgnoreReturnValue
  public static int checkIndex(int index, int size) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(
          index < 0
              ? String.format("index (%s) must be non-negative", index)
              : String.format("index (%s) must be less than size (%s)", index, size));
    }
    return index;
  }

  @CanIgnoreReturnValue
  public static int checkIndex(int index, int size, @Nullable String errorMessage) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(String.valueOf(errorMessage));
    }
    return index;
  }

  @CanIgnoreReturnValue
  public static <T> T checkNotNull(@Nullable T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  @CanIgnoreReturnValue
  public static <T> T checkNotNull(@Nullable T reference, @Nullable String errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }

  public static void checkState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }

  public static void checkState(boolean expression, @Nullable String errorMessage) {
    if (!expression) {
      throw new IllegalStateException(String.valueOf(errorMessage));
    }
  }

  private Preconditions() {}
}
