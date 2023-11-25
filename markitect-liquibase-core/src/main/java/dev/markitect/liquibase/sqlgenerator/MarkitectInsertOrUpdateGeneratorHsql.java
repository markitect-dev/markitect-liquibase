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
import static java.util.stream.Collectors.joining;

import dev.markitect.liquibase.statement.InsertOrUpdateExecutablePreparedStatement.PreparedSql;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import liquibase.database.Database;
import liquibase.sqlgenerator.core.InsertOrUpdateGeneratorHsql;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class MarkitectInsertOrUpdateGeneratorHsql extends InsertOrUpdateGeneratorHsql
    implements MarkitectInsertOrUpdateGenerator {
  @Override
  public int getPriority() {
    return super.getPriority() + 5;
  }

  @Override
  public PreparedSql prepareInsertOrUpdateSql(
      Database database, InsertOrUpdateStatement statement, List<ColumnValue> columnValues) {
    checkNotNull(database);
    checkNotNull(statement);
    checkNotNull(columnValues);
    var keyValues = columnValues.stream().filter(ColumnValue::isPrimaryKeyColumn).toList();
    var updateValues =
        columnValues.stream()
            .filter(columnValue -> !columnValue.isPrimaryKeyColumn() && columnValue.isAllowUpdate())
            .toList();
    String sql =
        "MERGE INTO "
            + database.escapeTableName(
                statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
            + " USING (VALUES (1)) ON "
            + keyValues.stream()
                .map(
                    columnValue ->
                        database.escapeObjectName(columnValue.getName(), Column.class)
                            + " = "
                            + columnValueToSql(database, columnValue))
                .collect(joining(" AND "))
            + " WHEN NOT MATCHED THEN INSERT ("
            + columnValues.stream()
                .map(ColumnValue::getName)
                .map(columnName -> database.escapeObjectName(columnName, Column.class))
                .collect(joining(", "))
            + ") VALUES ("
            + columnValues.stream()
                .map(columnValue -> columnValueToSql(database, columnValue))
                .collect(joining(", "))
            + ") WHEN MATCHED THEN UPDATE SET "
            + updateValues.stream()
                .map(
                    columnValue ->
                        database.escapeObjectName(columnValue.getName(), Column.class)
                            + " = "
                            + columnValueToSql(database, columnValue))
                .collect(joining(", "));
    var bindColumns =
        Stream.of(keyValues, columnValues, updateValues)
            .flatMap(Collection::stream)
            .filter(ColumnValue::isBindValue)
            .map(ColumnValue::getColumn)
            .toList();
    return new PreparedSql(
        sql,
        bindColumns,
        new Table(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
  }
}
