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

import dev.markitect.liquibase.statement.SetIdentityInsertStatement;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.structure.core.Table;

public class SetIdentityInsertGenerator extends AbstractSqlGenerator<SetIdentityInsertStatement> {
  @Override
  public boolean supports(SetIdentityInsertStatement statement, Database database) {
    checkNotNull(statement);
    checkNotNull(database);
    return database instanceof MSSQLDatabase;
  }

  @Override
  public ValidationErrors validate(
      SetIdentityInsertStatement statement,
      Database database,
      SqlGeneratorChain<SetIdentityInsertStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    var errors = new ValidationErrors();
    errors.checkRequiredField("tableName", statement.getTableName());
    errors.checkRequiredField("value", statement.getValue());
    return errors;
  }

  @Override
  public Sql[] generateSql(
      SetIdentityInsertStatement statement,
      Database database,
      SqlGeneratorChain<SetIdentityInsertStatement> sqlGeneratorChain) {
    checkNotNull(statement);
    checkNotNull(database);
    checkNotNull(sqlGeneratorChain);
    String sql =
        "SET IDENTITY_INSERT "
            + database.escapeTableName(
                statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
            + " "
            + statement.getValue();
    return new Sql[] {
      new UnparsedSql(
          sql,
          new Table(
              statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()))
    };
  }
}
