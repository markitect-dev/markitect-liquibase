/*
 * Copyright 2023-2025 Markitect
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

import dev.markitect.liquibase.statement.DropDatabaseStatement;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import org.jspecify.annotations.Nullable;

@DatabaseChange(
    name = "dropDatabase",
    description = "Drops a database",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "catalog")
@SuppressWarnings("squid:S2160")
public class DropDatabaseChange extends AbstractChange {
  private @Nullable String databaseName;

  @DatabaseChangeProperty(
      description = "Name of the database to drop",
      mustEqualExisting = "catalog")
  @SuppressWarnings("unused")
  public @Nullable String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(@Nullable String databaseName) {
    this.databaseName = databaseName;
  }

  @Override
  public String getConfirmationMessage() {
    return "Database " + databaseName + " dropped";
  }

  @Override
  public SqlStatement[] generateStatements(Database database) {
    checkNotNull(database);
    var statement = new DropDatabaseStatement();
    statement.setDatabaseName(databaseName);
    return new SqlStatement[] {statement};
  }
}
