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

package dev.markitect.liquibase;

import static org.assertj.core.api.Assertions.assertThat;

import liquibase.Scope;
import liquibase.ScopeManager;
import liquibase.ThreadLocalScopeManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScopeManagerHelperTests {
  @Mock private MockedConstruction<ThreadLocalScopeManager> mockedThreadLocalScopeManager;
  @Mock private MockedStatic<Scope> mockedScope;
  @Captor private ArgumentCaptor<ScopeManager> scopeManagerCaptor;

  @Test
  void getInstance() {
    // when
    var instance = ScopeManagerHelper.getInstance();

    // then
    assertThat(instance).isNotNull();
  }

  @Test
  @SuppressWarnings("DirectInvocationOnMock")
  void useThreadLocalScopeManager() throws Exception {
    // given
    var constructor = ScopeManagerHelper.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    var instance = constructor.newInstance();

    // when
    instance.useThreadLocalScopeManager();
    instance.useThreadLocalScopeManager();

    // then
    assertThat(mockedThreadLocalScopeManager.constructed()).hasSize(1);
    mockedScope.verify(() -> Scope.setScopeManager(scopeManagerCaptor.capture()));
    assertThat(scopeManagerCaptor.getAllValues())
        .isEqualTo(mockedThreadLocalScopeManager.constructed());
    mockedScope.verifyNoMoreInteractions();
  }
}
