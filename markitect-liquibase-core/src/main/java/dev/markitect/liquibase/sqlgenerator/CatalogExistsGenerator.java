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

package dev.markitect.liquibase.sqlgenerator;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.markitect.liquibase.statement.CatalogExistsStatement;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;

public class CatalogExistsGenerator extends AbstractSqlGenerator<CatalogExistsStatement> {
  @Override
  public boolean supports(CatalogExistsStatement statement, Database database) {
    checkNotNull(statement);
    checkNotNull(database);
    return database instanceof MSSQLDatabase || database instanceof PostgresDatabase;
  }

  @Override
  public ValidationErrors validate(
      CatalogExistsStatement statement,
      Database database,
      SqlGeneratorChain<CatalogExistsStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    var errors = new ValidationErrors();
    errors.checkRequiredField("catalogName", statement.getCatalogName());
    return errors;
  }

  @Override
  public Sql[] generateSql(
      CatalogExistsStatement statement,
      Database database,
      SqlGeneratorChain<CatalogExistsStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    String sql;
    if (database instanceof MSSQLDatabase) {
      sql =
          "SELECT CAST(CASE WHEN DB_ID(N'"
              + database.escapeStringForDatabase(
                  database.correctObjectName(statement.getCatalogName(), Catalog.class))
              + "') IS NOT NULL THEN 1 ELSE 0 END AS "
              + database.escapeDataTypeName("bit")
              + ")";
    } else {
      sql =
          "SELECT EXISTS(SELECT 1 FROM "
              + database.escapeTableName(null, "pg_catalog", "pg_database")
              + " WHERE "
              + database.escapeObjectName("datname", Column.class)
              + " = '"
              + database.escapeStringForDatabase(
                  database.correctObjectName(statement.getCatalogName(), Catalog.class))
              + "')";
    }
    return new Sql[] {new UnparsedSql(sql)};
  }
}
