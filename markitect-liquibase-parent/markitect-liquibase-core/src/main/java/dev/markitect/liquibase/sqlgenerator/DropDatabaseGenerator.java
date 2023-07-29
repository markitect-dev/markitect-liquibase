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

import dev.markitect.liquibase.statement.DropDatabaseStatement;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.structure.core.Catalog;

public class DropDatabaseGenerator extends AbstractSqlGenerator<DropDatabaseStatement> {
  @Override
  public boolean supports(DropDatabaseStatement statement, Database database) {
    checkNotNull(statement);
    checkNotNull(database);
    return database instanceof MSSQLDatabase || database instanceof PostgresDatabase;
  }

  @Override
  public ValidationErrors validate(
      DropDatabaseStatement statement,
      Database database,
      SqlGeneratorChain<DropDatabaseStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    var errors = new ValidationErrors();
    errors.checkRequiredField("databaseName", statement.getDatabaseName());
    return errors;
  }

  @Override
  public Sql[] generateSql(
      DropDatabaseStatement statement,
      Database database,
      SqlGeneratorChain<DropDatabaseStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    String sql =
        "DROP DATABASE " + database.escapeObjectName(statement.getDatabaseName(), Catalog.class);
    return new Sql[] {new UnparsedSql(sql, new Catalog(statement.getDatabaseName()))};
  }
}
