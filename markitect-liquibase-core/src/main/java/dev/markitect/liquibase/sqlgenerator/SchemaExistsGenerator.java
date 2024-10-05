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

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;

import com.google.errorprone.annotations.Var;
import dev.markitect.liquibase.statement.SchemaExistsStatement;
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
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;

public class SchemaExistsGenerator extends AbstractSqlGenerator<SchemaExistsStatement> {
  @Override
  public boolean supports(SchemaExistsStatement statement, Database database) {
    checkNotNull(statement);
    checkNotNull(database);
    return database instanceof H2Database
        || database instanceof HsqlDatabase
        || database instanceof MSSQLDatabase
        || database instanceof PostgresDatabase;
  }

  @Override
  public ValidationErrors validate(
      SchemaExistsStatement statement,
      Database database,
      SqlGeneratorChain<SchemaExistsStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    var errors = new ValidationErrors();
    errors.checkRequiredField("schemaName", statement.getSchemaName());
    return errors;
  }

  @Override
  @SuppressWarnings("squid:S1192")
  public Sql[] generateSql(
      SchemaExistsStatement statement,
      Database database,
      SqlGeneratorChain<SchemaExistsStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    @Var String sql;
    if (database instanceof H2Database || database instanceof HsqlDatabase) {
      sql =
          "SELECT EXISTS(SELECT 1 FROM "
              + database.escapeViewName(
                  statement.getCatalogName(), "INFORMATION_SCHEMA", "SCHEMATA")
              + " WHERE "
              + database.escapeObjectName("SCHEMA_NAME", Column.class)
              + " = '"
              + database.escapeStringForDatabase(
                  database.correctObjectName(statement.getSchemaName(), Schema.class))
              + "')";
      if (database instanceof HsqlDatabase) {
        sql +=
            " FROM "
                + database.escapeViewName(
                    statement.getCatalogName(), "INFORMATION_SCHEMA", "SYSTEM_USERS");
      }
    } else if (database instanceof MSSQLDatabase) {
      sql =
          "SELECT CAST(CASE WHEN EXISTS (SELECT 1 FROM "
              + database.escapeViewName(statement.getCatalogName(), "sys", "schemas")
              + " WHERE "
              + database.escapeObjectName("name", Column.class)
              + " = N'"
              + database.escapeStringForDatabase(
                  database.correctObjectName(statement.getSchemaName(), Schema.class))
              + "') THEN 1 ELSE 0 END AS "
              + database.escapeDataTypeName("bit")
              + ")";
    } else {
      sql =
          "SELECT EXISTS(SELECT 1 FROM "
              + database.escapeTableName(statement.getCatalogName(), "pg_catalog", "pg_namespace")
              + " WHERE "
              + database.escapeObjectName("nspname", Column.class)
              + " = '"
              + database.escapeStringForDatabase(
                  database.correctObjectName(statement.getSchemaName(), Schema.class))
              + "')";
    }
    return new Sql[] {new UnparsedSql(sql)};
  }
}
