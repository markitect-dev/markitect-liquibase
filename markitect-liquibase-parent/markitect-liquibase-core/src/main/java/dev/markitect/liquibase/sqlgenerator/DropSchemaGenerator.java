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

package dev.markitect.liquibase.sqlgenerator;

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;

import dev.markitect.liquibase.statement.DropSchemaStatement;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;

public class DropSchemaGenerator extends AbstractSqlGenerator<DropSchemaStatement> {
  @Override
  public boolean supports(DropSchemaStatement statement, Database database) {
    checkNotNull(statement);
    checkNotNull(database);
    return database instanceof H2Database
        || database instanceof HsqlDatabase
        || database instanceof MSSQLDatabase
        || database instanceof PostgresDatabase;
  }

  @Override
  @SuppressWarnings("DuplicatedCode")
  public ValidationErrors validate(
      DropSchemaStatement statement,
      Database database,
      SqlGeneratorChain<DropSchemaStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    ValidationErrors errors = new ValidationErrors();
    if (!(database instanceof MSSQLDatabase)) {
      errors.checkDisallowedField(
          "catalogName", statement.getCatalogName(), database, database.getClass());
    }
    errors.checkRequiredField("schemaName", statement.getSchemaName());
    return errors;
  }

  @Override
  public Sql[] generateSql(
      DropSchemaStatement statement,
      Database database,
      SqlGeneratorChain<DropSchemaStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    String sql =
        "DROP SCHEMA " + database.escapeObjectName(statement.getSchemaName(), Schema.class);
    if (database instanceof MSSQLDatabase && statement.getCatalogName() != null) {
      sql =
          "USE "
              + database.escapeObjectName(statement.getCatalogName(), Catalog.class)
              + "; "
              + sql;
      sql = "EXEC sp_executesql N'" + database.escapeStringForDatabase(sql) + "'";
    }
    return new Sql[] {
      new UnparsedSql(sql, new Schema(statement.getCatalogName(), statement.getSchemaName()))
    };
  }
}
