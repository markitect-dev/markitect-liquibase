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

package dev.markitect.liquibase.exception;

import static org.assertj.core.api.Assertions.assertThat;

import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.Test;

class UncheckedDatabaseExceptionTests {
  @Test
  @SuppressWarnings("AssertBetweenInconvertibleTypes")
  void newInstance() {
    // given
    final String message = "message";
    final var cause = new DatabaseException();

    // when
    var exception = new UncheckedDatabaseException(message, cause);

    // then
    assertThat(exception).hasMessage(message).cause().isSameAs(cause);

    // when
    exception = new UncheckedDatabaseException(cause);

    // then
    assertThat(exception).hasMessage(cause.toString()).cause().isSameAs(cause);
  }
}
