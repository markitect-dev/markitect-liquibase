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

package dev.markitect.liquibase.change;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.PostgresDatabase;

@DatabaseChange(
    name = "loadData",
    description = "Loads data from a CSV file into an existing table",
    priority = ChangeMetaData.PRIORITY_DEFAULT + 5,
    appliesTo = "table")
@SuppressWarnings("squid:S110")
public class MarkitectLoadDataChange extends LoadDataChange {
  @Override
  public boolean supports(Database database) {
    return database instanceof H2Database
        || database instanceof HsqlDatabase
        || database instanceof MSSQLDatabase
        || database instanceof PostgresDatabase;
  }

  @Override
  public boolean generateStatementsVolatile(Database database) {
    return getColumns().stream().anyMatch(column -> column.getType() == null);
  }
}
