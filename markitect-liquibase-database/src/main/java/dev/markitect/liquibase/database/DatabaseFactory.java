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

import liquibase.database.Database;
import liquibase.exception.DatabaseException;

@FunctionalInterface
public interface DatabaseFactory<D extends Database> {
  static <T extends Database> DatabaseFactory<T> toDatabaseFactory(Class<T> databaseClass) {
    checkNotNull(databaseClass);
    return () -> {
      try {
        return databaseClass.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new DatabaseException(e);
      }
    };
  }

  D get() throws DatabaseException;
}
