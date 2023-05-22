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

package dev.markitect.liquibase.database.hsqldb;

import dev.markitect.liquibase.database.Databases;
import liquibase.structure.DatabaseObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HsqlDatabase extends liquibase.database.core.HsqlDatabase {
  @Override
  public int getPriority() {
    return super.getPriority() + 5;
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
