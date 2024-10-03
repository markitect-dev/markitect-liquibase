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

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import dev.markitect.liquibase.statement.CatalogExistsStatement;
import dev.markitect.liquibase.statement.SchemaExistsStatement;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import org.jspecify.annotations.Nullable;

public final class Databases {
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean catalogExists(Database database, @Nullable String catalogName)
      throws DatabaseException {
    checkNotNull(database);
    if (database.isDefaultCatalog(catalogName)) {
      return true;
    }
    if (database instanceof H2Database || database instanceof HsqlDatabase) {
      return false;
    }
    var statement = new CatalogExistsStatement();
    statement.setCatalogName(catalogName);
    return isTrue(
        Scope.getCurrentScope()
            .getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForObject(statement, Boolean.class));
  }

  public static boolean schemaExists(
      Database database, @Nullable String catalogName, @Nullable String schemaName)
      throws DatabaseException, InvalidExampleException {
    checkNotNull(database);
    if (database.isDefaultSchema(catalogName, schemaName)) {
      return true;
    }
    if (database.isDefaultCatalog(catalogName)) {
      return SnapshotGeneratorFactory.getInstance()
          .has(new Schema(catalogName, schemaName), database);
    }
    if (!catalogExists(database, catalogName)) {
      return false;
    }
    var statement = new SchemaExistsStatement();
    statement.setCatalogName(catalogName);
    statement.setSchemaName(schemaName);
    return isTrue(
        Scope.getCurrentScope()
            .getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForObject(statement, Boolean.class));
  }

  private Databases() {}
}
