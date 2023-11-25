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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class VerifyTests {
  @Test
  void verify() {
    // when
    var thrown = Assertions.catchThrowable(() -> Verify.verify(true));

    // then
    assertThat(thrown).isNull();

    // when
    thrown = Assertions.catchThrowable(() -> Verify.verify(true, "errorMessage"));

    // then
    assertThat(thrown).isNull();
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void verifyThrowsVerifyException() {
    // when
    var thrown = catchThrowable(() -> Verify.verify(false));

    // then
    assertThat(thrown).isInstanceOf(VerifyException.class);

    // when
    thrown = catchThrowable(() -> Verify.verify(false, "errorMessage"));

    // then
    assertThat(thrown).isInstanceOf(VerifyException.class).hasMessage("errorMessage");
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void verifyNotNull() {
    // given
    var reference = new Object();

    // when
    var result = Verify.verifyNotNull(reference);

    // then
    assertThat(result).isSameAs(reference);

    // when
    result = Verify.verifyNotNull(reference, "errorMessage");

    // then
    assertThat(result).isSameAs(reference);
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void verifyNotNullThrowsVerifyException() {
    // when
    var thrown = catchThrowable(() -> Verify.verifyNotNull(null));

    // then
    assertThat(thrown).isInstanceOf(VerifyException.class);

    // when
    thrown = catchThrowable(() -> Verify.verifyNotNull(null, "errorMessage"));

    // then
    assertThat(thrown).isInstanceOf(VerifyException.class).hasMessage("errorMessage");
  }
}
