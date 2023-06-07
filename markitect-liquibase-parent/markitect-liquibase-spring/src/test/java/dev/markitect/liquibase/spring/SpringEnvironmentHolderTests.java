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

package dev.markitect.liquibase.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SpringEnvironmentHolderTests {
  @BeforeEach
  void setUp() {
    SpringEnvironmentHolder.removeEnvironment();
  }

  @AfterEach
  void tearDown() {
    SpringEnvironmentHolder.removeEnvironment();
  }

  @Test
  void shouldSetEnvironment() {
    // given
    var environment = new MockEnvironment();

    // when
    SpringEnvironmentHolder.setEnvironment(environment);

    // then
    assertThat(SpringEnvironmentHolder.getEnvironment()).isSameAs(environment);
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void shouldFailToSetNulLEnvironment() {
    // when
    var thrown = catchThrowable(() -> SpringEnvironmentHolder.setEnvironment(null));

    // then
    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Environment must not be null");
  }

  @Test
  void shouldRemoveEnvironment() {
    // given
    SpringEnvironmentHolder.setEnvironment(new MockEnvironment());

    // when
    SpringEnvironmentHolder.removeEnvironment();

    // then
    assertThat(SpringEnvironmentHolder.getEnvironment()).isNull();
  }
}
