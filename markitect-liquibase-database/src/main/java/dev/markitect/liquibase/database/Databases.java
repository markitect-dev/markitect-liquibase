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

import static dev.markitect.liquibase.structure.Structures.isCatalogOrSchemaType;
import static dev.markitect.liquibase.util.Preconditions.checkNotNull;
import static dev.markitect.liquibase.util.Strings.isIllegalIdentifier;
import static liquibase.util.BooleanUtil.isTrue;

import java.util.Locale;
import java.util.function.BiPredicate;
import liquibase.GlobalConfiguration;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.structure.DatabaseObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Databases {
  public static <D extends AbstractJdbcDatabase> @Nullable String escapeObjectName(
      D database,
      BiPredicate<String, Class<? extends DatabaseObject>> mustQuoteObjectName,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType) {
    checkNotNull(database);
    checkNotNull(mustQuoteObjectName);
    checkNotNull(objectType);
    if (objectName == null) {
      return null;
    }
    return mustQuoteObjectName.test(objectName, objectType)
        ? database.quoteObject(database.correctObjectName(objectName, objectType), objectType)
        : objectName;
  }

  public static <D extends AbstractJdbcDatabase> boolean mustQuoteObjectName(
      D database,
      @Nullable Boolean unquotedObjectsAreUppercased,
      String objectName,
      Class<? extends DatabaseObject> objectType) {
    checkNotNull(database);
    checkNotNull(objectName);
    checkNotNull(objectType);
    return database.getObjectQuotingStrategy() == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
        || isIllegalIdentifier(objectName)
        || database.isReservedWord(objectName)
        || (unquotedObjectsAreUppercased != null
            && isCatalogOrSchemaType(objectType)
            && isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())
            && !objectName.equals(
                unquotedObjectsAreUppercased
                    ? objectName.toUpperCase(Locale.US)
                    : objectName.toLowerCase(Locale.US)));
  }

  public static <D extends AbstractJdbcDatabase> @Nullable String correctObjectName(
      D database,
      @Nullable Boolean unquotedObjectsAreUppercased,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType) {
    checkNotNull(database);
    checkNotNull(objectType);
    if (objectName == null) {
      return null;
    }
    return unquotedObjectsAreUppercased == null
            || database.getObjectQuotingStrategy() == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
            || (isCatalogOrSchemaType(objectType)
                && isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue()))
        ? objectName
        : unquotedObjectsAreUppercased
            ? objectName.toUpperCase(Locale.US)
            : objectName.toLowerCase(Locale.US);
  }

  private Databases() {}
}
