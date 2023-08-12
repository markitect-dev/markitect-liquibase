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

package dev.markitect.liquibase.database;

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;
import static dev.markitect.liquibase.structure.Structures.isCatalogOrSchemaType;
import static dev.markitect.liquibase.util.Strings.isIllegalIdentifier;
import static liquibase.util.BooleanUtil.isTrue;

import java.util.Locale;
import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MarkitectDatabase extends Database {
  @Override
  default @Nullable String correctObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    if (objectName == null) {
      return null;
    }
    @Nullable Boolean unquotedObjectsAreUppercased;
    if ((unquotedObjectsAreUppercased = getUnquotedObjectsAreUppercased()) == null
        || getObjectQuotingStrategy() == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
        || (isCatalogOrSchemaType(objectType)
            && isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue()))) {
      return objectName;
    }
    if (isTrue(unquotedObjectsAreUppercased)) {
      return objectName.toUpperCase(Locale.US);
    }
    return objectName.toLowerCase(Locale.US);
  }

  @Override
  default @Nullable String escapeObjectName(
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
  default @Nullable String escapeObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    if (objectName == null) {
      return null;
    }
    return mustQuoteObjectName(objectName, objectType)
        ? quoteObject(correctObjectName(objectName, objectType), objectType)
        : objectName;
  }

  default boolean mustQuoteObjectName(
      String objectName, Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectName);
    checkNotNull(objectType);
    @Nullable Boolean unquotedObjectsAreUppercased;
    return getObjectQuotingStrategy() == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
        || isIllegalIdentifier(objectName)
        || isReservedWord(objectName)
        || ((unquotedObjectsAreUppercased = getUnquotedObjectsAreUppercased()) != null
            && isCatalogOrSchemaType(objectType)
            && isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())
            && !objectName.equals(
                isTrue(unquotedObjectsAreUppercased)
                    ? objectName.toUpperCase(Locale.US)
                    : objectName.toLowerCase(Locale.US)));
  }

  @Nullable String quoteObject(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType);

  @Nullable Boolean getUnquotedObjectsAreUppercased();

  default @Nullable String toCatalogNameToUse(@Nullable String catalogName) {
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

  default @Nullable String toSchemaNameToUse(
      @Nullable String catalogName, @Nullable String schemaName) {
    if ((isTrue(GlobalConfiguration.INCLUDE_CATALOG_IN_SPECIFICATION.getCurrentValue())
            && getOutputDefaultCatalog()
            && !supportsOmittedInnerSchemaName())
        || getOutputDefaultSchema()
        || !isDefaultSchema(catalogName, schemaName)) {
      if (schemaName != null) {
        return schemaName;
      }
      if (isDefaultCatalog(catalogName)) {
        return getDefaultSchemaName();
      }
    }
    return null;
  }

  default boolean supportsOmittedInnerSchemaName() {
    return false;
  }
}
