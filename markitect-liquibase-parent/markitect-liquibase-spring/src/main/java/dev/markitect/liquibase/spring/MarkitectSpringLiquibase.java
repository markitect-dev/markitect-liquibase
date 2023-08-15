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

import java.sql.Connection;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;

public class MarkitectSpringLiquibase extends DataSourceClosingSpringLiquibase {
  protected boolean outputDefaultCatalog;
  protected boolean outputDefaultSchema;

  public MarkitectSpringLiquibase() {
    setCloseDataSourceOnceMigrated(false);
  }

  @Override
  protected Database createDatabase(@Nullable Connection c, ResourceAccessor resourceAccessor)
      throws DatabaseException {
    var database = super.createDatabase(c, resourceAccessor);
    database.setOutputDefaultCatalog(outputDefaultCatalog);
    database.setOutputDefaultSchema(outputDefaultSchema);
    return database;
  }

  public void setOutputDefaultCatalog(boolean outputDefaultCatalog) {
    this.outputDefaultCatalog = outputDefaultCatalog;
  }

  public void setOutputDefaultSchema(boolean outputDefaultSchema) {
    this.outputDefaultSchema = outputDefaultSchema;
  }
}
