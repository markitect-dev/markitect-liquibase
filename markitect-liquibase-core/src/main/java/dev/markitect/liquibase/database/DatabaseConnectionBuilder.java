/*
 * Copyright 2023-2026 Markitect
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

import static com.google.common.base.Preconditions.checkState;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import liquibase.Scope;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import org.jspecify.annotations.Nullable;

/** Builds configured Liquibase database connections. */
public final class DatabaseConnectionBuilder {
  /** Creates a new database connection builder. */
  public static DatabaseConnectionBuilder newBuilder() {
    return new DatabaseConnectionBuilder();
  }

  private @Nullable String url;
  private @Nullable String username;
  private @Nullable String password;
  private @Nullable String driver;

  /** Sets the JDBC URL. */
  @CanIgnoreReturnValue
  public DatabaseConnectionBuilder url(@Nullable String url) {
    this.url = url;
    return this;
  }

  /** Sets the database username. */
  @CanIgnoreReturnValue
  public DatabaseConnectionBuilder username(@Nullable String username) {
    this.username = username;
    return this;
  }

  /** Sets the database password. */
  @CanIgnoreReturnValue
  public DatabaseConnectionBuilder password(@Nullable String password) {
    this.password = password;
    return this;
  }

  /** Sets the JDBC driver. */
  @CanIgnoreReturnValue
  public DatabaseConnectionBuilder driver(@Nullable String driver) {
    this.driver = driver;
    return this;
  }

  /** Builds the configured database connection. */
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
