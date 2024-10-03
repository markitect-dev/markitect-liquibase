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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import dev.markitect.liquibase.statement.InsertOrUpdateExecutablePreparedStatement.PreparedSql;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import org.jspecify.annotations.Nullable;

public interface MarkitectInsertOrUpdateGenerator extends SqlGenerator<InsertOrUpdateStatement> {
  default PreparedSql prepareSql(
      Database database, InsertOrUpdateStatement statement, List<? extends ColumnConfig> columns) {
    var primaryKeyColumnNames =
        Arrays.stream(statement.getPrimaryKey().split(","))
            .map(String::trim)
            .collect(toCollection(LinkedHashSet::new));
    var columnValues =
        columns.stream()
            .map(
                column ->
                    new ColumnValue(
                        column,
                        column.getName(),
                        column.getValueObject(),
                        statement.getAllowColumnUpdate(column.getName()),
                        primaryKeyColumnNames.contains(column.getName()),
                        !(column.getValueObject() instanceof DatabaseFunction)))
            .toList();
    return isTrue(statement.getOnlyUpdate())
        ? prepareUpdateSql(database, statement, columnValues)
        : prepareInsertOrUpdateSql(database, statement, columnValues);
  }

  default PreparedSql prepareUpdateSql(
      Database database, InsertOrUpdateStatement statement, List<ColumnValue> columnValues) {
    var updateValues =
        columnValues.stream()
            .filter(columnValue -> !columnValue.isPrimaryKeyColumn() && columnValue.isAllowUpdate())
            .toList();
    var whereValues = columnValues.stream().filter(ColumnValue::isPrimaryKeyColumn).toList();
    String sql =
        "UPDATE "
            + database.escapeTableName(
                statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
            + " SET "
            + updateValues.stream()
                .map(
                    columnValue ->
                        database.escapeObjectName(columnValue.getName(), Column.class)
                            + " = "
                            + columnValueToSql(database, columnValue))
                .collect(joining(", "))
            + " WHERE "
            + whereValues.stream()
                .map(
                    columnValue ->
                        database.escapeObjectName(columnValue.getName(), Column.class)
                            + " = "
                            + columnValueToSql(database, columnValue))
                .collect(joining(" AND "));
    var bindColumns =
        Stream.of(updateValues, whereValues)
            .flatMap(Collection::stream)
            .filter(ColumnValue::isBindValue)
            .map(ColumnValue::getColumn)
            .toList();
    return new PreparedSql(
        sql,
        bindColumns,
        new Table(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
  }

  PreparedSql prepareInsertOrUpdateSql(
      Database database, InsertOrUpdateStatement statement, List<ColumnValue> columnValues);

  default String columnValueToSql(Database database, ColumnValue columnValue) {
    return columnValue.isBindValue()
        ? "?"
        : Optional.ofNullable(columnValue.getValueObject())
            .map(
                valueObject ->
                    DataTypeFactory.getInstance()
                        .fromObject(valueObject, database)
                        .objectToSql(valueObject, database))
            .orElse("NULL");
  }

  @SuppressWarnings({"ClassCanBeRecord", "squid:S6206"})
  class ColumnValue {
    private final @Nullable ColumnConfig column;
    private final String name;
    private final @Nullable Object valueObject;
    private final boolean allowUpdate;
    private final boolean primaryKeyColumn;
    private final boolean bindValue;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public ColumnValue(
        @Nullable ColumnConfig column,
        String name,
        @Nullable Object valueObject,
        boolean allowUpdate,
        boolean primaryKeyColumn,
        boolean bindValue) {
      this.column = column;
      this.name = checkNotNull(name);
      this.valueObject = valueObject;
      this.allowUpdate = allowUpdate;
      this.primaryKeyColumn = primaryKeyColumn;
      this.bindValue = bindValue;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public @Nullable ColumnConfig getColumn() {
      return column;
    }

    public String getName() {
      return name;
    }

    public @Nullable Object getValueObject() {
      return valueObject;
    }

    public boolean isAllowUpdate() {
      return allowUpdate;
    }

    public boolean isPrimaryKeyColumn() {
      return primaryKeyColumn;
    }

    public boolean isBindValue() {
      return bindValue;
    }
  }
}
