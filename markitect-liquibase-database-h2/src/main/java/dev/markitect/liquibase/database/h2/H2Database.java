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

package dev.markitect.liquibase.database.h2;

import static dev.markitect.liquibase.structure.Structures.isCatalogOrSchemaType;
import static dev.markitect.liquibase.util.Preconditions.checkNotNull;
import static dev.markitect.liquibase.util.Strings.isIllegalIdentifier;
import static liquibase.util.BooleanUtil.isTrue;

import java.util.Locale;
import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.structure.DatabaseObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public class H2Database extends liquibase.database.core.H2Database {
  @Override
  public int getPriority() {
    return super.getPriority() + 5;
  }

  @Override
  public @Nullable String escapeObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    if (objectName == null) {
      return null;
    }
    if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
        || mustQuoteObjectName(objectName, objectType)) {
      return quoteObject(correctObjectName(objectName, objectType), objectType);
    }
    return objectName;
  }

  @Override
  protected boolean mustQuoteObjectName(
      String objectName, Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectName);
    checkNotNull(objectType);
    return (isCatalogOrSchemaType(objectType)
            && (isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())
                || getSchemaAndCatalogCase()
                    == CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE))
        || isIllegalIdentifier(objectName)
        || isReservedWord(objectName);
  }

  @Override
  public @Nullable String correctObjectName(
      @Nullable String objectName, Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    if (objectName == null) {
      return null;
    }
    return quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
            || (isCatalogOrSchemaType(objectType)
                && (isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())
                    || getSchemaAndCatalogCase()
                        == CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE))
        ? objectName
        : objectName.toUpperCase(Locale.US);
  }
}
