/*
 * Copyright 2023-2024 Markitect
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PreconditionsTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # index | size
                0 |    1
                1 |    2
          """,
      delimiter = '|')
  @SuppressWarnings("DataFlowIssue")
  void checkIndex(int index, int size) {
    // when
    int result = Preconditions.checkIndex(index, size);

    // then
    assertThat(result).isEqualTo(index);

    // when
    result = Preconditions.checkIndex(index, size, "errorMessage");

    // then
    assertThat(result).isEqualTo(index);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # index | size
               -1 |    0
                0 |    0
                1 |    1
          """,
      delimiter = '|')
  void checkIndex_throwsIndexOutOfBoundsException(int index, int size) {
    // when
    var thrown = catchThrowable(() -> Preconditions.checkIndex(index, size));

    // then
    assertThat(thrown).isInstanceOf(IndexOutOfBoundsException.class);

    // when
    thrown = catchThrowable(() -> Preconditions.checkIndex(index, size, "errorMessage"));

    // then
    assertThat(thrown).isInstanceOf(IndexOutOfBoundsException.class).hasMessage("errorMessage");
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void checkNotNull() {
    // given
    var reference = new Object();

    // when
    var result = Preconditions.checkNotNull(reference);

    // then
    assertThat(result).isSameAs(reference);

    // when
    result = Preconditions.checkNotNull(reference, "errorMessage");

    // then
    assertThat(result).isSameAs(reference);
  }

  @Test
  @SuppressWarnings({"DataFlowIssue", "NullAway"})
  void checkNotNull_throwsNullPointerException() {
    // when
    var thrown = catchThrowable(() -> Preconditions.checkNotNull(null));

    // then
    assertThat(thrown).isInstanceOf(NullPointerException.class);

    // when
    thrown = catchThrowable(() -> Preconditions.checkNotNull(null, "errorMessage"));

    // then
    assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessage("errorMessage");
  }

  @Test
  void checkState() {
    // when
    var thrown =
        catchThrowable(
            () -> {
              Preconditions.checkState(true);
              Preconditions.checkState(true, "errorMessage");
            });

    // then
    assertThat(thrown).isNull();
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void checkState_throwsIllegalStateException() {
    // when
    var thrown = catchThrowable(() -> Preconditions.checkState(false));

    // then
    assertThat(thrown).isInstanceOf(IllegalStateException.class);

    // when
    thrown = catchThrowable(() -> Preconditions.checkState(false, "errorMessage"));

    // then
    assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("errorMessage");
  }
}
