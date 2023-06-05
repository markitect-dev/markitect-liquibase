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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import dev.markitect.liquibase.ScopeManagerHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class SpringLiquibaseBeanPostProcessorTest {
  private final MockEnvironment environment = new MockEnvironment();
  private final SpringLiquibaseBeanPostProcessor springLiquibaseBeanPostProcessor =
      new SpringLiquibaseBeanPostProcessor(environment);

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void doesNotUseThreadLocalScopeManager() {
    try (var mocked = mockStatic(ScopeManagerHelper.class)) {
      // when
      springLiquibaseBeanPostProcessor.postProcessBeforeInstantiation(
          MarkitectSpringLiquibase.class, "liquibase");

      // then
      mocked.verify(ScopeManagerHelper::useThreadLocalScopeManager, never());
    }
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void usesThreadLocalScopeManager() {
    try (var mocked = mockStatic(ScopeManagerHelper.class)) {
      // given
      environment.setProperty("markitect.liquibase.use-thread-local-scope-manager", "true");

      // when
      springLiquibaseBeanPostProcessor.postProcessBeforeInstantiation(
          MarkitectSpringLiquibase.class, "liquibase");

      // then
      mocked.verify(ScopeManagerHelper::useThreadLocalScopeManager);
    }
  }
}
