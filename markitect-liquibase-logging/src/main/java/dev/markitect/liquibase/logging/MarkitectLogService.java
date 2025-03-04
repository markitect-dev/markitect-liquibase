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

package dev.markitect.liquibase.logging;

import static com.google.common.base.Preconditions.checkNotNull;

import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogService;

public class MarkitectLogService extends AbstractLogService {
  @Override
  public int getPriority() {
    return PRIORITY_SPECIALIZED;
  }

  @Override
  @SuppressWarnings({"rawtypes", "RedundantSuppression"})
  public Logger getLog(Class clazz) {
    checkNotNull(clazz);
    return LoggerAdapter.getLogger(clazz.getName());
  }
}
