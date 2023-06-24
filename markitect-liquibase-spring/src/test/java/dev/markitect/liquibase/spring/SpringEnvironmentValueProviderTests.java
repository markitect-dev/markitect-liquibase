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

import liquibase.configuration.ProvidedValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class SpringEnvironmentValueProviderTests {
  @Mock private MockedStatic<SpringEnvironmentHolder> mockedSpringEnvironmentHolder;

  private final MockEnvironment environment = new MockEnvironment();
  private final SpringEnvironmentValueProvider springEnvironmentValueProvider =
      new SpringEnvironmentValueProvider();

  @Test
  void precedenceShouldBe250() {
    // when
    int precedence = springEnvironmentValueProvider.getPrecedence();

    // then
    assertThat(precedence).isEqualTo(250);
  }

  @Test
  @SuppressWarnings({"DirectInvocationOnMock", "ResultOfMethodCallIgnored", "RedundantSuppression"})
  void shouldReturnProvidedValue() {
    // given
    mockedSpringEnvironmentHolder
        .when(SpringEnvironmentHolder::getEnvironment)
        .thenReturn(environment);
    environment.setProperty(
        "markitect.liquibase.properties.liquibase.databaseChangeLogTableName", "databasechangelog");

    // when
    ProvidedValue actual =
        springEnvironmentValueProvider.getProvidedValue(
            "liquibase.databaseChangelogTableName", "liquibase.databaseChangeLogTableName");

    // then
    assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(
            new ProvidedValue(
                "liquibase.databaseChangeLogTableName",
                "liquibase.databaseChangelogTableName",
                "databasechangelog",
                "SpringEnvironmentValueProvider",
                springEnvironmentValueProvider));
  }

  @Test
  @SuppressWarnings({"DirectInvocationOnMock", "ResultOfMethodCallIgnored", "RedundantSuppression"})
  void shouldNotReturnProvidedValue() {
    // given
    mockedSpringEnvironmentHolder
        .when(SpringEnvironmentHolder::getEnvironment)
        .thenReturn(environment);
    environment.setProperty(
        "markitect.liquibase.properties.liquibase.databaseChangeLogTableName", "databasechangelog");

    // when
    ProvidedValue actual =
        springEnvironmentValueProvider.getProvidedValue("liquibase.databaseChangelogTableName");

    // then
    assertThat(actual).isNull();
  }
}
