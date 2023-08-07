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

package dev.markitect.liquibase.database.mssql;

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;
import static liquibase.util.BooleanUtil.isTrue;

import dev.markitect.liquibase.database.MarkitectDatabase;
import liquibase.GlobalConfiguration;
import liquibase.database.core.MSSQLDatabase;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MarkitectMssqlDatabase extends MSSQLDatabase implements MarkitectDatabase {
  @Override
  public int getPriority() {
    return super.getPriority() + 5;
  }

  @Override
  public @Nullable String correctObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    return MarkitectDatabase.super.correctObjectName(objectName, objectType);
  }

  @Override
  public @Nullable String escapeObjectName(
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    if (Index.class.isAssignableFrom(objectType)) {
      return escapeObjectName(objectName, objectType);
    }
    @Nullable String catalogNameToUse = toCatalogNameToUse(catalogName);
    @Nullable String schemaNameToUse = toSchemaNameToUse(catalogName, schemaName);
    return (catalogNameToUse != null ? escapeObjectName(catalogNameToUse, Catalog.class) + "." : "")
        + (schemaNameToUse != null ? escapeObjectName(schemaNameToUse, Schema.class) : "")
        + (catalogNameToUse != null || schemaNameToUse != null ? "." : "")
        + escapeObjectName(objectName, objectType);
  }

  @Override
  public @Nullable String escapeObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    return MarkitectDatabase.super.escapeObjectName(objectName, objectType);
  }

  @Override
  public boolean mustQuoteObjectName(
      String objectName, Class<? extends DatabaseObject> objectType) {
    return MarkitectDatabase.super.mustQuoteObjectName(objectName, objectType);
  }

  @Override
  @SuppressWarnings("squid:S1185")
  public String quoteObject(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    return super.quoteObject(objectName, objectType);
  }

  @Override
  public @Nullable Boolean getUnquotedObjectsAreUppercased() {
    return unquotedObjectsAreUppercased;
  }

  private @Nullable String toCatalogNameToUse(@Nullable String catalogName) {
    if ((isTrue(GlobalConfiguration.INCLUDE_CATALOG_IN_SPECIFICATION.getCurrentValue())
            && getOutputDefaultCatalog())
        || !isDefaultCatalog(catalogName)) {
      if (catalogName != null) {
        return catalogName;
      }
      return getDefaultCatalogName();
    }
    return null;
  }

  private @Nullable String toSchemaNameToUse(
      @Nullable String catalogName, @Nullable String schemaName) {
    if (getOutputDefaultSchema() || !isDefaultSchema(catalogName, schemaName)) {
      if (schemaName != null) {
        return schemaName;
      }
      if (isDefaultCatalog(catalogName)) {
        return getDefaultSchemaName();
      }
    }
    return null;
  }
}
