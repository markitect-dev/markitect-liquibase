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

import com.google.errorprone.annotations.Var;
import org.junit.jupiter.api.Test;

class VerifyExceptionTests {
  @Test
  void newInstance() {
    // given
    String message = "message";
    var cause = new Exception();

    // when
    @Var var exception = new VerifyException();

    // then
    assertThat(exception).hasMessage(null).hasNoCause();

    // when
    exception = new VerifyException(message);

    // then
    assertThat(exception).hasMessage(message).hasNoCause();

    // when
    exception = new VerifyException(message, cause);

    // then
    assertThat(exception).hasMessage(message).cause().isSameAs(cause);

    // when
    exception = new VerifyException(cause);

    // then
    assertThat(exception).hasMessage(cause.toString()).cause().isSameAs(cause);
  }
}
