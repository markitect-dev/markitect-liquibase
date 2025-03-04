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

import dev.markitect.liquibase.statement.SetIdentityInsertStatement;
import dev.markitect.liquibase.statement.SetIdentityInsertStatement.IdentityInsert;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.statement.SqlStatement;
import org.jspecify.annotations.Nullable;

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
    priority = ChangeMetaData.PRIORITY_DEFAULT + 10,
    appliesTo = "table")
@SuppressWarnings({"squid:S110", "squid:S2160"})
public class MarkitectLoadUpdateDataChangeMssql extends MarkitectLoadUpdateDataChange {
  private @Nullable Boolean identityInsert;

  @DatabaseChangeProperty(description = "Whether to set IDENTITY_INSERT")
  public @Nullable Boolean getIdentityInsert() {
    return identityInsert;
  }

  @SuppressWarnings("unused")
  public void setIdentityInsert(@Nullable Boolean identityInsert) {
    this.identityInsert = identityInsert;
  }

  @Override
  public boolean supports(Database database) {
    checkNotNull(database);
    return database instanceof MSSQLDatabase;
  }

  @Override
  @SuppressWarnings("DuplicatedCode")
  public SqlStatement[] generateStatements(Database database) {
    if (isTrue(getIdentityInsert())) {
      return Stream.of(
              Stream.of(
                  new SetIdentityInsertStatement(
                      getCatalogName(), getSchemaName(), getTableName(), IdentityInsert.ON)),
              Arrays.stream(super.generateStatements(database)),
              Stream.of(
                  new SetIdentityInsertStatement(
                      getCatalogName(), getSchemaName(), getTableName(), IdentityInsert.OFF)))
          .flatMap(Function.identity())
          .toArray(SqlStatement[]::new);
    }
    return super.generateStatements(database);
  }
}
