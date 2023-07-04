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

import dev.markitect.liquibase.base.Nullable;
import dev.markitect.liquibase.base.Verify;
import java.util.function.UnaryOperator;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;

public final class DatabaseBuilder<D extends Database> {
  public static <T extends Database> DatabaseBuilder<T> of(Class<T> databaseClass) {
    checkNotNull(databaseClass);
    return new DatabaseBuilder<>(databaseClass, null, null, null, null, null);
  }

  private final Class<D> databaseClass;
  private final @Nullable DatabaseConnectionBuilder databaseConnectionBuilder;
  private final @Nullable UnaryOperator<OfflineConnectionBuilder> offlineConnectionCustomizer;
  private final @Nullable ObjectQuotingStrategy objectQuotingStrategy;
  private final @Nullable Boolean outputDefaultCatalog;
  private final @Nullable Boolean outputDefaultSchema;

  private DatabaseBuilder(
      Class<D> databaseClass,
      @Nullable DatabaseConnectionBuilder databaseConnectionBuilder,
      @Nullable UnaryOperator<OfflineConnectionBuilder> offlineConnectionCustomizer,
      @Nullable ObjectQuotingStrategy objectQuotingStrategy,
      @Nullable Boolean outputDefaultCatalog,
      @Nullable Boolean outputDefaultSchema) {
    this.databaseClass = checkNotNull(databaseClass);
    this.databaseConnectionBuilder = databaseConnectionBuilder;
    this.offlineConnectionCustomizer = offlineConnectionCustomizer;
    this.objectQuotingStrategy = objectQuotingStrategy;
    this.outputDefaultCatalog = outputDefaultCatalog;
    this.outputDefaultSchema = outputDefaultSchema;
  }

  public DatabaseBuilder<D> withDatabaseConnection(
      @Nullable DatabaseConnectionBuilder databaseConnectionBuilder) {
    return new DatabaseBuilder<>(
        databaseClass,
        databaseConnectionBuilder,
        null,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> withOfflineConnection() {
    return withOfflineConnection(UnaryOperator.identity());
  }

  public DatabaseBuilder<D> withOfflineConnection(
      @Nullable UnaryOperator<OfflineConnectionBuilder> offlineConnectionCustomizer) {
    return new DatabaseBuilder<>(
        databaseClass,
        null,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> withObjectQuotingStrategy(
      @Nullable ObjectQuotingStrategy objectQuotingStrategy) {
    return new DatabaseBuilder<>(
        databaseClass,
        databaseConnectionBuilder,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> withOutputDefaultCatalog(@Nullable Boolean outputDefaultCatalog) {
    return new DatabaseBuilder<>(
        databaseClass,
        databaseConnectionBuilder,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> withOutputDefaultSchema(@Nullable Boolean outputDefaultSchema) {
    return new DatabaseBuilder<>(
        databaseClass,
        databaseConnectionBuilder,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public D build() {
    D database;
    try {
      database = databaseClass.getConstructor().newInstance();
    } catch (ReflectiveOperationException | RuntimeException e) {
      throw new IllegalStateException(e);
    }
    if (databaseConnectionBuilder != null) {
      DatabaseConnection databaseConnection = databaseConnectionBuilder.build();
      database.setConnection(databaseConnection);
    } else if (offlineConnectionCustomizer != null) {
      MarkitectOfflineConnection offlineConnection =
          offlineConnectionCustomizer
              .andThen(Verify::verifyNotNull)
              .apply(OfflineConnectionBuilder.of().withShortName(database.getShortName()))
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
