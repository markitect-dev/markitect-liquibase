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
import static org.mockito.Mockito.mock;

import dev.markitect.liquibase.ScopeManagerHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class SpringLiquibaseBeanPostProcessorTests {
  @Mock private MockedStatic<ScopeManagerHelper> mockedScopeManagerHelper;
  @Mock private MockedStatic<SpringEnvironmentHolder> mockedSpringEnvironmentHolder;
  private final MockEnvironment environment = new MockEnvironment();
  private final SpringLiquibaseBeanPostProcessor springLiquibaseBeanPostProcessor =
      new SpringLiquibaseBeanPostProcessor(environment);

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # beanClass                                             | beanName
          dev.markitect.liquibase.spring.MarkitectSpringLiquibase | liquibase
          liquibase.integration.spring.SpringLiquibase            | liquibase
          """,
      delimiter = '|')
  @SuppressWarnings("DirectInvocationOnMock")
  void postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
    // given
    environment.setProperty("markitect.liquibase.use-thread-local-scope-manager", "true");

    // when
    Object actual =
        springLiquibaseBeanPostProcessor.postProcessBeforeInstantiation(beanClass, beanName);

    // then
    mockedScopeManagerHelper.verify(ScopeManagerHelper::useThreadLocalScopeManager);
    mockedScopeManagerHelper.verifyNoMoreInteractions();
    assertThat(actual).isNull();
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # beanClass          | beanName
          javax.sql.DataSource | dataSource
          """,
      delimiter = '|')
  @SuppressWarnings("DirectInvocationOnMock")
  void shouldNotUseThreadLocalScopeManagerBeforeInstantiationForNonLiquibaseBean(
      Class<?> beanClass, String beanName) {
    // given
    environment.setProperty("markitect.liquibase.use-thread-local-scope-manager", "true");

    // when
    Object actual =
        springLiquibaseBeanPostProcessor.postProcessBeforeInstantiation(beanClass, beanName);

    // then
    mockedScopeManagerHelper.verifyNoInteractions();
    assertThat(actual).isNull();
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # beanClass                                             | beanName
          dev.markitect.liquibase.spring.MarkitectSpringLiquibase | liquibase
          liquibase.integration.spring.SpringLiquibase            | liquibase
          """,
      delimiter = '|')
  @SuppressWarnings("DirectInvocationOnMock")
  void shouldNotUseThreadLocalScopeManagerBeforeInstantiationWithoutEnvironmentProperty(
      Class<?> beanClass, String beanName) {
    // when
    Object actual =
        springLiquibaseBeanPostProcessor.postProcessBeforeInstantiation(beanClass, beanName);

    // then
    mockedScopeManagerHelper.verifyNoInteractions();
    assertThat(actual).isNull();
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # beanClass                                             | beanName
          dev.markitect.liquibase.spring.MarkitectSpringLiquibase | liquibase
          liquibase.integration.spring.SpringLiquibase            | liquibase
          """,
      delimiter = '|')
  @SuppressWarnings("DirectInvocationOnMock")
  void shouldSetEnvironmentBeforeInitialization(Class<?> beanClass, String beanName) {
    // given
    Object bean = mock(beanClass);

    // when
    Object actual =
        springLiquibaseBeanPostProcessor.postProcessBeforeInitialization(bean, beanName);

    // then
    mockedSpringEnvironmentHolder.verify(() -> SpringEnvironmentHolder.setEnvironment(environment));
    mockedSpringEnvironmentHolder.verifyNoMoreInteractions();
    assertThat(actual).isSameAs(bean);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # beanClass          | beanName
          javax.sql.DataSource | dataSource
          """,
      delimiter = '|')
  @SuppressWarnings("DirectInvocationOnMock")
  void shouldNotSetEnvironmentBeforeInitialization(Class<?> beanClass, String beanName) {
    // given
    Object bean = mock(beanClass);

    // when
    Object actual =
        springLiquibaseBeanPostProcessor.postProcessBeforeInitialization(bean, beanName);

    // then
    mockedSpringEnvironmentHolder.verifyNoInteractions();
    assertThat(actual).isSameAs(bean);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # beanClass                                             | beanName
          dev.markitect.liquibase.spring.MarkitectSpringLiquibase | liquibase
          liquibase.integration.spring.SpringLiquibase            | liquibase
          """,
      delimiter = '|')
  @SuppressWarnings("DirectInvocationOnMock")
  void shouldRemoveEnvironmentAfterInitialization(Class<?> beanClass, String beanName) {
    // given
    Object bean = mock(beanClass);

    // when
    Object actual = springLiquibaseBeanPostProcessor.postProcessAfterInitialization(bean, beanName);

    // then
    mockedSpringEnvironmentHolder.verify(SpringEnvironmentHolder::removeEnvironment);
    mockedSpringEnvironmentHolder.verifyNoMoreInteractions();
    assertThat(actual).isSameAs(bean);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # beanClass          | beanName
          javax.sql.DataSource | dataSource
          """,
      delimiter = '|')
  @SuppressWarnings("DirectInvocationOnMock")
  void shouldNotRemoveEnvironmentAfterInitialization(Class<?> beanClass, String beanName) {
    // given
    Object bean = mock(beanClass);

    // when
    Object actual = springLiquibaseBeanPostProcessor.postProcessAfterInitialization(bean, beanName);

    // then
    mockedSpringEnvironmentHolder.verifyNoInteractions();
    assertThat(actual).isSameAs(bean);
  }
}
