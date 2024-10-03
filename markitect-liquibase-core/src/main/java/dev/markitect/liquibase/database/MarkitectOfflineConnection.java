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

package dev.markitect.liquibase.database;

import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;
import org.jspecify.annotations.Nullable;

public class MarkitectOfflineConnection extends OfflineConnection {
  private @Nullable String catalog;
  private @Nullable String schema;

  public MarkitectOfflineConnection(String url, ResourceAccessor resourceAccessor) {
    super(url, resourceAccessor);
  }

  @Override
  public @Nullable String getCatalog() throws DatabaseException {
    return catalog != null ? catalog : super.getCatalog();
  }

  public void setCatalog(@Nullable String catalog) {
    this.catalog = catalog;
  }

  @Override
  public @Nullable String getSchema() {
    return schema != null ? schema : super.getSchema();
  }

  public void setSchema(@Nullable String schema) {
    this.schema = schema;
  }
}
