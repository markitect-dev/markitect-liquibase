/*
 * Copyright 2023-2026 Markitect
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

import liquibase.statement.AbstractSqlStatement;
import org.jspecify.annotations.Nullable;

/** Represents a SQL Server IDENTITY_INSERT toggle statement. */
public class SetIdentityInsertStatement extends AbstractSqlStatement {
  private final @Nullable String catalogName;
  private final @Nullable String schemaName;
  private final @Nullable String tableName;
  private final @Nullable IdentityInsert value;

  /** Creates a statement that toggles SQL Server IDENTITY_INSERT for a table. */
  public SetIdentityInsertStatement(
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String tableName,
      @Nullable IdentityInsert value) {
    this.catalogName = catalogName;
    this.schemaName = schemaName;
    this.tableName = tableName;
    this.value = value;
  }

  public @Nullable String getCatalogName() {
    return catalogName;
  }

  public @Nullable String getSchemaName() {
    return schemaName;
  }

  public @Nullable String getTableName() {
    return tableName;
  }

  public @Nullable IdentityInsert getValue() {
    return value;
  }

  @Override
  public boolean skipOnUnsupported() {
    return true;
  }

  /** Defines SQL Server IDENTITY_INSERT toggle values. */
  public enum IdentityInsert {
    ON,
    OFF
  }
}
