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

package dev.markitect.liquibase.base;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

public final class Verify {
  public static void verify(boolean expression) {
    if (!expression) {
      throw new VerifyException();
    }
  }

  public static void verify(boolean expression, @Nullable String errorMessage) {
    if (!expression) {
      throw new VerifyException(String.valueOf(errorMessage));
    }
  }

  @CanIgnoreReturnValue
  public static <T> T verifyNotNull(@Nullable T reference) {
    if (reference == null) {
      throw new VerifyException();
    }
    return reference;
  }

  @CanIgnoreReturnValue
  public static <T> T verifyNotNull(@Nullable T reference, @Nullable String errorMessage) {
    if (reference == null) {
      throw new VerifyException(String.valueOf(errorMessage));
    }
    return reference;
  }

  private Verify() {}
}
