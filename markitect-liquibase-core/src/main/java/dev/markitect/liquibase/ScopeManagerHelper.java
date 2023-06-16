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

import java.util.concurrent.locks.ReentrantLock;
import liquibase.Scope;
import liquibase.ThreadLocalScopeManager;

public class ScopeManagerHelper {
  private static final ReentrantLock initLock = new ReentrantLock();
  private static volatile boolean initialized;

  public static void useThreadLocalScopeManager() {
    initLock.lock();
    try {
      if (!initialized) {
        Scope.setScopeManager(new ThreadLocalScopeManager());
        initialized = true;
      }
    } finally {
      initLock.unlock();
    }
  }

  private ScopeManagerHelper() {}
}
