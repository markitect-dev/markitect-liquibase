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

package dev.markitect.liquibase;

import dev.markitect.liquibase.base.Runnables;
import dev.markitect.liquibase.base.Suppliers;
import java.util.function.Supplier;
import liquibase.Scope;
import liquibase.ThreadLocalScopeManager;

public final class ScopeManagerHelper {
  private static final Supplier<ScopeManagerHelper> instance =
      Suppliers.memoize(ScopeManagerHelper::new);

  public static ScopeManagerHelper getInstance() {
    return instance.get();
  }

  @SuppressWarnings("squid:S5164")
  private final ThreadLocal<Runnable> useThreadLocalScopeManager =
      ThreadLocal.withInitial(
          () -> Runnables.runOnce(() -> Scope.setScopeManager(new ThreadLocalScopeManager())));

  public void useThreadLocalScopeManager() {
    useThreadLocalScopeManager.get().run();
  }

  private ScopeManagerHelper() {}
}
