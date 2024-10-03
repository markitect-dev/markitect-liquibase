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

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;

import dev.markitect.liquibase.base.Verify;
import java.util.function.UnaryOperator;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import org.jspecify.annotations.Nullable;

public final class DatabaseBuilder<D extends Database> {
  public static <T extends Database> DatabaseBuilder<T> newBuilder(Class<T> databaseClass) {
    checkNotNull(databaseClass);
    return new DatabaseBuilder<>(databaseClass);
  }

  private final Class<D> databaseClass;
  private @Nullable DatabaseConnectionBuilder databaseConnectionBuilder;
  private @Nullable UnaryOperator<OfflineConnectionBuilder> offlineConnectionCustomizer;
  private @Nullable ObjectQuotingStrategy objectQuotingStrategy;
  private @Nullable Boolean outputDefaultCatalog;
  private @Nullable Boolean outputDefaultSchema;

  private DatabaseBuilder(Class<D> databaseClass) {
    this.databaseClass = checkNotNull(databaseClass);
  }

  public DatabaseBuilder<D> databaseConnection(
      @Nullable DatabaseConnectionBuilder databaseConnectionBuilder) {
    this.databaseConnectionBuilder = databaseConnectionBuilder;
    this.offlineConnectionCustomizer = null;
    return this;
  }

  public DatabaseBuilder<D> offlineConnection() {
    return offlineConnection(UnaryOperator.identity());
  }

  public DatabaseBuilder<D> offlineConnection(
      @Nullable UnaryOperator<OfflineConnectionBuilder> offlineConnectionCustomizer) {
    this.databaseConnectionBuilder = null;
    this.offlineConnectionCustomizer = offlineConnectionCustomizer;
    return this;
  }

  public DatabaseBuilder<D> objectQuotingStrategy(
      @Nullable ObjectQuotingStrategy objectQuotingStrategy) {
    this.objectQuotingStrategy = objectQuotingStrategy;
    return this;
  }

  public DatabaseBuilder<D> outputDefaultCatalog(@Nullable Boolean outputDefaultCatalog) {
    this.outputDefaultCatalog = outputDefaultCatalog;
    return this;
  }

  public DatabaseBuilder<D> outputDefaultSchema(@Nullable Boolean outputDefaultSchema) {
    this.outputDefaultSchema = outputDefaultSchema;
    return this;
  }

  public D build() {
    D database;
    try {
      database = databaseClass.getConstructor().newInstance();
    } catch (ReflectiveOperationException | RuntimeException e) {
      throw new IllegalStateException(e);
    }
    if (databaseConnectionBuilder != null) {
      var databaseConnection = databaseConnectionBuilder.build();
      database.setConnection(databaseConnection);
    } else if (offlineConnectionCustomizer != null) {
      var offlineConnection =
          offlineConnectionCustomizer
              .andThen(Verify::verifyNotNull)
              .apply(OfflineConnectionBuilder.newBuilder(database.getShortName()))
              .build();
      database.setConnection(offlineConnection);
    }
    if (objectQuotingStrategy != null) {
      database.setObjectQuotingStrategy(objectQuotingStrategy);
    }
    if (outputDefaultCatalog != null) {
      database.setOutputDefaultCatalog(outputDefaultCatalog);
    }
    if (outputDefaultSchema != null) {
      database.setOutputDefaultSchema(outputDefaultSchema);
    }
    return database;
  }
}
