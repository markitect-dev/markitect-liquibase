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

import static dev.markitect.liquibase.base.Preconditions.checkState;

import dev.markitect.liquibase.base.Nullable;
import liquibase.Scope;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;

public final class DatabaseConnectionBuilder {
  private static final DatabaseConnectionBuilder SINGLETON =
      new DatabaseConnectionBuilder(null, null, null, null);

  public static DatabaseConnectionBuilder of() {
    return SINGLETON;
  }

  private final @Nullable String url;
  private final @Nullable String username;
  private final @Nullable String password;
  private final @Nullable String driver;

  private DatabaseConnectionBuilder(
      @Nullable String url,
      @Nullable String username,
      @Nullable String password,
      @Nullable String driver) {
    this.url = url;
    this.username = username;
    this.password = password;
    this.driver = driver;
  }

  public DatabaseConnectionBuilder withUrl(@Nullable String url) {
    return new DatabaseConnectionBuilder(url, username, password, driver);
  }

  public DatabaseConnectionBuilder withUsername(@Nullable String username) {
    return new DatabaseConnectionBuilder(url, username, password, driver);
  }

  public DatabaseConnectionBuilder withPassword(@Nullable String password) {
    return new DatabaseConnectionBuilder(url, username, password, driver);
  }

  public DatabaseConnectionBuilder withDriver(@Nullable String driver) {
    return new DatabaseConnectionBuilder(url, username, password, driver);
  }

  public DatabaseConnection build() {
    checkState(url != null);
    try {
      return DatabaseFactory.getInstance()
          .openConnection(
              url,
              username,
              password,
              driver,
              null,
              null,
              null,
              Scope.getCurrentScope().getResourceAccessor());
    } catch (DatabaseException | RuntimeException e) {
      throw new IllegalStateException(e);
    }
  }
}
