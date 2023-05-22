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

import static dev.markitect.liquibase.util.Preconditions.checkNotNull;
import static liquibase.util.BooleanUtil.isTrue;

import dev.markitect.liquibase.database.Databases;
import liquibase.GlobalConfiguration;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MSSQLDatabase extends liquibase.database.core.MSSQLDatabase {
  @Override
  public int getPriority() {
    return super.getPriority() + 5;
  }

  @Override
  public String escapeObjectName(
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    if (Index.class.isAssignableFrom(objectType)) {
      return escapeObjectName(objectName, objectType);
    }
    String catalogNameToUse =
        (isTrue(GlobalConfiguration.INCLUDE_CATALOG_IN_SPECIFICATION.getCurrentValue())
                    && getOutputDefaultCatalog())
                || !isDefaultCatalog(catalogName)
            ? (catalogName != null ? catalogName : getDefaultCatalogName())
            : null;
    String schemaNameToUse =
        getOutputDefaultSchema() || !isDefaultSchema(catalogName, schemaName)
            ? (schemaName != null
                ? schemaName
                : (isDefaultCatalog(catalogName) ? getDefaultSchemaName() : null))
            : null;
    return (catalogNameToUse != null ? escapeObjectName(catalogNameToUse, Catalog.class) : "")
        + (catalogNameToUse != null ? "." : "")
        + (schemaNameToUse != null ? escapeObjectName(schemaNameToUse, Schema.class) : "")
        + (catalogNameToUse != null || schemaNameToUse != null ? "." : "")
        + escapeObjectName(objectName, objectType);
  }

  @Override
  public @Nullable String escapeObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    return Databases.escapeObjectName(this, this::mustQuoteObjectName, objectName, objectType);
  }

  @Override
  protected boolean mustQuoteObjectName(
      String objectName, Class<? extends DatabaseObject> objectType) {
    return Databases.mustQuoteObjectName(
        this, unquotedObjectsAreUppercased, objectName, objectType);
  }

  @Override
  public @Nullable String correctObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    return Databases.correctObjectName(this, unquotedObjectsAreUppercased, objectName, objectType);
  }
}
