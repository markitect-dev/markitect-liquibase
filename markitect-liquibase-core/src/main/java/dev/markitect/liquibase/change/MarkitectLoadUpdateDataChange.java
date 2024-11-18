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

package dev.markitect.liquibase.change;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import dev.markitect.liquibase.sqlgenerator.MarkitectInsertOrUpdateGenerator;
import dev.markitect.liquibase.statement.InsertOrUpdateExecutablePreparedStatement;
import java.util.List;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.change.core.LoadUpdateDataChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.resource.ResourceAccessor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.InsertOrUpdateStatement;

@DatabaseChange(
    name = "loadUpdateData",
    description =
        """
        Loads or updates data from a CSV file into an existing table. Differs from loadData by \
        issuing a SQL batch that checks for the existence of a record. If found, the record is \
        UPDATEd, else the record is INSERTed. Also, generates DELETE statements for a rollback.

        A value of NULL in a cell will be converted to a database NULL rather than the string \
        'NULL'\
        """,
    priority = ChangeMetaData.PRIORITY_DEFAULT + 5,
    appliesTo = "table")
@SuppressWarnings("squid:S110")
public class MarkitectLoadUpdateDataChange extends LoadUpdateDataChange {
  @Override
  public boolean supports(Database database) {
    checkNotNull(database);
    var statement = createStatement(catalogName, schemaName, tableName);
    return SqlGeneratorFactory.getInstance().getGenerators(statement, database).stream()
        .anyMatch(MarkitectInsertOrUpdateGenerator.class::isInstance);
  }

  @Override
  public boolean generateStatementsVolatile(Database database) {
    return getColumns().stream().anyMatch(column -> column.getType() == null);
  }

  @Override
  protected boolean hasPreparedStatementsImplemented() {
    return true;
  }

  @Override
  protected InsertOrUpdateStatement createStatement(
      String catalogName, String schemaName, String tableName) {
    return new InsertOrUpdateStatement(
        catalogName, schemaName, tableName, getPrimaryKey(), isTrue(getOnlyUpdate()));
  }

  @Override
  protected InsertOrUpdateExecutablePreparedStatement createPreparedStatement(
      Database database,
      String catalogName,
      String schemaName,
      String tableName,
      List<LoadDataColumnConfig> columns,
      ChangeSet changeSet,
      ResourceAccessor resourceAccessor) {
    var statement = createStatement(catalogName, schemaName, tableName);
    var generator =
        SqlGeneratorFactory.getInstance().getGenerators(statement, database).stream()
            .filter(MarkitectInsertOrUpdateGenerator.class::isInstance)
            .map(MarkitectInsertOrUpdateGenerator.class::cast)
            .findFirst()
            .orElseThrow();
    var preparedSql = generator.prepareSql(database, statement, columns);
    return new InsertOrUpdateExecutablePreparedStatement(
        database,
        catalogName,
        schemaName,
        tableName,
        columns,
        changeSet,
        resourceAccessor,
        preparedSql);
  }
}
