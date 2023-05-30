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

import liquibase.database.core.H2Database;
import liquibase.structure.DatabaseObject;

class MarkitectH2Database extends H2Database implements MarkitectDatabase {
  @Override
  public int getPriority() {
    return super.getPriority() + 5;
  }

  @Override
  public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
    return MarkitectDatabase.super.escapeObjectName(objectName, objectType);
  }

  @Override
  public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
    return MarkitectDatabase.super.correctObjectName(objectName, objectType);
  }

  @Override
  public boolean mustQuoteObjectName(
      String objectName, Class<? extends DatabaseObject> objectType) {
    return MarkitectDatabase.super.mustQuoteObjectName(objectName, objectType);
  }

  @Override
  public Boolean getUnquotedObjectsAreUppercased() {
    return unquotedObjectsAreUppercased;
  }
}
