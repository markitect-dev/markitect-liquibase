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

import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

final class SpringEnvironmentHolder {
  private static final InheritableThreadLocal<Environment> environmentHolder =
      new InheritableThreadLocal<>();

  static @Nullable Environment getEnvironment() {
    return environmentHolder.get();
  }

  static void setEnvironment(Environment environment) {
    Assert.notNull(environment, "Environment must not be null");
    environmentHolder.set(environment);
  }

  static void removeEnvironment() {
    environmentHolder.remove();
  }

  private SpringEnvironmentHolder() {}
}
