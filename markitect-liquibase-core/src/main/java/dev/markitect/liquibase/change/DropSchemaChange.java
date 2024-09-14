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

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;

import dev.markitect.liquibase.statement.DropSchemaStatement;
import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import org.checkerframework.checker.nullness.qual.Nullable;

@DatabaseChange(
    name = "dropSchema",
    description = "Drops a schema",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "schema")
@SuppressWarnings("squid:S2160")
public class DropSchemaChange extends AbstractChange {
  private @Nullable String catalogName;
  private @Nullable String schemaName;

  @DatabaseChangeProperty(
      description = "Name of the database catalog",
      mustEqualExisting = "schema.catalog")
  @SuppressWarnings("unused")
  public @Nullable String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(@Nullable String catalogName) {
    this.catalogName = catalogName;
  }

  @DatabaseChangeProperty(
      description = "Name of the database schema to drop",
      mustEqualExisting = "schema")
  @SuppressWarnings("unused")
  public @Nullable String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(@Nullable String schemaName) {
    this.schemaName = schemaName;
  }

  @Override
  protected Change[] createInverses() {
    var inverse = new CreateSchemaChange();
    inverse.setCatalogName(catalogName);
    inverse.setSchemaName(schemaName);
    return new Change[] {inverse};
  }

  @Override
  public String getConfirmationMessage() {
    return "Schema " + (catalogName != null ? catalogName + "." : "") + schemaName + " dropped";
  }

  @Override
  public SqlStatement[] generateStatements(Database database) {
    checkNotNull(database);
    var statement = new DropSchemaStatement();
    statement.setCatalogName(catalogName);
    statement.setSchemaName(schemaName);
    return new SqlStatement[] {statement};
  }
}
