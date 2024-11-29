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

package dev.markitect.liquibase.database;

import static com.google.common.base.Preconditions.checkNotNull;
import static dev.markitect.liquibase.structure.Structures.isCatalogOrSchemaType;
import static dev.markitect.liquibase.util.Strings.isIllegalIdentifier;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Locale;
import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import org.jspecify.annotations.Nullable;

public interface MarkitectDatabase extends Database {
  @Override
  default @Nullable String correctObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    if (objectName == null) {
      return null;
    }
    Boolean unquotedObjectsAreUppercased;
    if ((unquotedObjectsAreUppercased = getUnquotedObjectsAreUppercased()) == null
        || (supportsPreservingIdentifierCase(objectType)
            && (getObjectQuotingStrategy() == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
                || (isCatalogOrSchemaType(objectType)
                    && isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue()))))) {
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
    String catalogNameToUse = toCatalogNameToUse(catalogName);
    String schemaNameToUse = toSchemaNameToUse(catalogName, schemaName);
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

  @SuppressFBWarnings("IMPROPER_UNICODE")
  default boolean mustQuoteObjectName(
      String objectName, Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectName);
    checkNotNull(objectType);
    Boolean unquotedObjectsAreUppercased;
    return getObjectQuotingStrategy() == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
        || isIllegalIdentifier(objectName)
        || isReservedWord(objectName)
        || ((unquotedObjectsAreUppercased = getUnquotedObjectsAreUppercased()) != null
            && supportsPreservingIdentifierCase(objectType)
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

  default boolean supportsPreservingIdentifierCase(Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    return true;
  }
}
