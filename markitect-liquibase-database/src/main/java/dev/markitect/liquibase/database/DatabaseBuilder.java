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

import static dev.markitect.liquibase.util.Preconditions.checkNotNull;
import static dev.markitect.liquibase.util.Preconditions.checkState;
import static dev.markitect.liquibase.util.Verify.verifyNotNull;

import dev.markitect.liquibase.util.Verify;
import java.util.function.UnaryOperator;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DatabaseBuilder<D extends Database> {
  private final DatabaseFactory<D> databaseFactory;
  private final ResourceAccessor resourceAccessor;
  private final @Nullable UnaryOperator<OfflineConnectionBuilder> offlineConnectionCustomizer;
  private final @Nullable ObjectQuotingStrategy objectQuotingStrategy;
  private final @Nullable Boolean outputDefaultCatalog;
  private final @Nullable Boolean outputDefaultSchema;

  private DatabaseBuilder(
      DatabaseFactory<D> databaseFactory,
      @Nullable ResourceAccessor resourceAccessor,
      @Nullable UnaryOperator<OfflineConnectionBuilder> offlineConnectionCustomizer,
      @Nullable ObjectQuotingStrategy objectQuotingStrategy,
      @Nullable Boolean outputDefaultCatalog,
      @Nullable Boolean outputDefaultSchema) {
    this.databaseFactory = checkNotNull(databaseFactory);
    this.resourceAccessor = resourceAccessor;
    this.offlineConnectionCustomizer = offlineConnectionCustomizer;
    this.objectQuotingStrategy = objectQuotingStrategy;
    this.outputDefaultCatalog = outputDefaultCatalog;
    this.outputDefaultSchema = outputDefaultSchema;
  }

  public <T extends Database> DatabaseBuilder<T> setDatabaseFactory(
      DatabaseFactory<T> databaseFactory) {
    checkNotNull(databaseFactory);
    return new DatabaseBuilder<>(
        databaseFactory,
        resourceAccessor,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> setResourceAccessor(@Nullable ResourceAccessor resourceAccessor) {
    return new DatabaseBuilder<>(
        databaseFactory,
        resourceAccessor,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> useOfflineConnection() {
    return useOfflineConnection(UnaryOperator.identity());
  }

  public DatabaseBuilder<D> useOfflineConnection(
      @Nullable UnaryOperator<OfflineConnectionBuilder> offlineConnectionCustomizer) {
    return new DatabaseBuilder<>(
        databaseFactory,
        resourceAccessor,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> setObjectQuotingStrategy(
      @Nullable ObjectQuotingStrategy objectQuotingStrategy) {
    return new DatabaseBuilder<>(
        databaseFactory,
        resourceAccessor,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> setOutputDefaultCatalog(@Nullable Boolean outputDefaultCatalog) {
    return new DatabaseBuilder<>(
        databaseFactory,
        resourceAccessor,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public DatabaseBuilder<D> setOutputDefaultSchema(@Nullable Boolean outputDefaultSchema) {
    return new DatabaseBuilder<>(
        databaseFactory,
        resourceAccessor,
        offlineConnectionCustomizer,
        objectQuotingStrategy,
        outputDefaultCatalog,
        outputDefaultSchema);
  }

  public D build() {
    checkState(offlineConnectionCustomizer == null || resourceAccessor != null);
    D database;
    try {
      database = verifyNotNull(databaseFactory.get());
    } catch (DatabaseException e) {
      throw new RuntimeException(e);
    }
    if (offlineConnectionCustomizer != null) {
      OfflineConnection connection =
          offlineConnectionCustomizer
              .andThen(Verify::verifyNotNull)
              .apply(
                  OfflineConnectionBuilder.of()
                      .setResourceAccessor(resourceAccessor)
                      .setShortName(database.getShortName()))
              .build();
      database.setConnection(connection);
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

  public static <D extends Database> DatabaseBuilder<D> of(DatabaseFactory<D> databaseFactory) {
    checkNotNull(databaseFactory);
    return new DatabaseBuilder<>(databaseFactory, null, null, null, null, null);
  }
}
