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

package dev.markitect.liquibase.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

class DatabaseConnectionBuilderTests {
  @Test
  void build_withoutUrl_throwsIllegalStateException() {
    // given
    var builder = DatabaseConnectionBuilder.newBuilder();

    // when
    var thrown = catchThrowable(builder::build);

    // then
    assertThat(thrown).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void build_withInvalidUrl_throwsIllegalStateException() {
    // given
    var builder = DatabaseConnectionBuilder.newBuilder().url("jdbc:");

    // when
    var thrown = catchThrowable(builder::build);

    // then
    assertThat(thrown).isInstanceOf(IllegalStateException.class);
  }
}
