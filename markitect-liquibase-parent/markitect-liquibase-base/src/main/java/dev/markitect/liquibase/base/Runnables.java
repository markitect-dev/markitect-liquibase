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

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;

import java.util.concurrent.ConcurrentHashMap;

public final class Runnables {
  public static Runnable runOnce(Runnable delegate) {
    return new RunOnceRunnable(delegate);
  }

  private Runnables() {}

  private static final class RunOnceRunnable implements Runnable {
    private final ConcurrentHashMap<Object, Object> cache = new ConcurrentHashMap<>(1);
    private final Runnable delegate;

    private RunOnceRunnable(Runnable delegate) {
      this.delegate = checkNotNull(delegate);
    }

    @Override
    public void run() {
      cache.computeIfAbsent(
          "",
          key -> {
            delegate.run();
            return key;
          });
    }
  }
}
