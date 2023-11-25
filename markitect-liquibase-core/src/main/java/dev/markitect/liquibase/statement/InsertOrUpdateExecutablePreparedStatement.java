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

package dev.markitect.liquibase.statement;

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.UnparsedSql;
import liquibase.statement.ExecutablePreparedStatementBase;
import liquibase.structure.DatabaseObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public class InsertOrUpdateExecutablePreparedStatement extends ExecutablePreparedStatementBase {
  private final PreparedSql preparedSql;

  @SuppressWarnings("squid:S107")
  public InsertOrUpdateExecutablePreparedStatement(
      Database database,
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String tableName,
      List<? extends ColumnConfig> columns,
      @Nullable ChangeSet changeSet,
      ResourceAccessor resourceAccessor,
      PreparedSql preparedSql) {
    super(
        checkNotNull(database),
        catalogName,
        schemaName,
        tableName,
        checkNotNull(columns),
        changeSet,
        checkNotNull(resourceAccessor));
    this.preparedSql = checkNotNull(preparedSql);
  }

  @Override
  public boolean continueOnError() {
    return false;
  }

  @Override
  protected String generateSql(List<ColumnConfig> cols) {
    checkNotNull(cols);
    cols.addAll(preparedSql.getBindColumns());
    return preparedSql.toSql();
  }

  @Override
  public List<? extends ColumnConfig> getColumns() {
    return preparedSql.getBindColumns();
  }

  public static class PreparedSql extends UnparsedSql {
    private final List<ColumnConfig> bindColumns;

    public PreparedSql(
        String sql,
        List<? extends ColumnConfig> bindColumns,
        DatabaseObject... affectedDatabaseObjects) {
      this(sql, bindColumns, ";", affectedDatabaseObjects);
    }

    @SuppressWarnings("Java9CollectionFactory")
    public PreparedSql(
        String sql,
        List<? extends ColumnConfig> bindColumns,
        String endDelimiter,
        DatabaseObject... affectedDatabaseObjects) {
      super(checkNotNull(sql), checkNotNull(endDelimiter), checkNotNull(affectedDatabaseObjects));
      this.bindColumns = unmodifiableList(new ArrayList<>(checkNotNull(bindColumns)));
    }

    public List<ColumnConfig> getBindColumns() {
      return bindColumns;
    }
  }
}
