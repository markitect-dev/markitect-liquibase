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

package dev.markitect.liquibase.database;

import java.util.Optional;
import liquibase.database.OfflineConnection;
import liquibase.resource.ResourceAccessor;

public class MarkitectOfflineConnection extends OfflineConnection {
  private String schema;

  @SuppressWarnings("unused")
  public MarkitectOfflineConnection(String url, ResourceAccessor resourceAccessor) {
    super(url, resourceAccessor);
  }

  @Override
  public String getSchema() {
    return Optional.ofNullable(schema).orElseGet(super::getSchema);
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }
}
