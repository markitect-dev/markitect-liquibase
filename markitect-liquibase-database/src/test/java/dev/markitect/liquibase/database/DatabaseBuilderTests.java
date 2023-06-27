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

import static dev.markitect.liquibase.database.DatabaseFactory.toDatabaseFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.markitect.liquibase.base.Nullable;
import dev.markitect.liquibase.base.VerifyException;
import dev.markitect.liquibase.exception.UncheckedDatabaseException;
import java.util.Map;
import java.util.Optional;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.jupiter.json.JsonSource;

class DatabaseBuilderTests {
  @ParameterizedTest
  @JsonSource(
      """
      [
        {
          databaseClass: 'liquibase.database.core.H2Database',
          useOfflineConnection: false,
          databaseParams: {},
          shortName: 'h2',
          productName: 'H2',
          majorVersion: 999,
          minorVersion: -1
        },
        {
          databaseClass: 'liquibase.database.core.H2Database',
          useOfflineConnection: true,
          databaseParams: {},
          shortName: 'h2',
          productName: 'Offline h2',
          majorVersion: 999,
          minorVersion: 999,
          schemaName: 'PUBLIC'
        },
        {
          databaseClass: 'liquibase.database.core.H2Database',
          useOfflineConnection: true,
          version: '1.4.200',
          databaseParams: {},
          shortName: 'h2',
          productName: 'Offline h2',
          majorVersion: 1,
          minorVersion: 4,
          schemaName: 'PUBLIC'
        },
        {
          databaseClass: 'liquibase.database.core.H2Database',
          outputDefaultCatalog: false,
          outputDefaultSchema: true,
          useOfflineConnection: false,
          databaseParams: {},
          shortName: 'h2',
          productName: 'H2',
          majorVersion: 999,
          minorVersion: -1
        },
        {
          databaseClass: 'liquibase.database.core.H2Database',
          quotingStrategy: 'QUOTE_ALL_OBJECTS',
          outputDefaultCatalog: false,
          outputDefaultSchema: true,
          useOfflineConnection: false,
          databaseParams: {},
          shortName: 'h2',
          productName: 'H2',
          majorVersion: 999,
          minorVersion: -1
        },
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          useOfflineConnection: true,
          catalog: 'Cat1',
          schema: 'Sch1',
          databaseParams: {},
          shortName: 'mssql',
          productName: 'Offline mssql',
          majorVersion: 999,
          minorVersion: 999,
          catalogName: 'Cat1',
          schemaName: 'Sch1'
        },
        {
          databaseClass: 'liquibase.database.core.PostgresDatabase',
          useOfflineConnection: false,
          databaseParams: {},
          shortName: 'postgresql',
          productName: 'PostgreSQL',
          majorVersion: 999,
          minorVersion: -1
        },
        {
          databaseClass: 'liquibase.database.core.PostgresDatabase',
          useOfflineConnection: true,
          databaseParams: {},
          shortName: 'postgresql',
          productName: 'Offline postgresql',
          majorVersion: 999,
          minorVersion: 999
        },
        {
          databaseClass: 'liquibase.database.core.PostgresDatabase',
          useOfflineConnection: true,
          catalog: 'Cat1',
          schema: 'Sch1',
          databaseParams: {},
          shortName: 'postgresql',
          productName: 'Offline postgresql',
          majorVersion: 999,
          minorVersion: 999,
          catalogName: 'Cat1',
          schemaName: 'Sch1'
        }
      ]
      """)
  void build(
      Class<? extends Database> databaseClass,
      @Nullable ObjectQuotingStrategy quotingStrategy,
      @Nullable Boolean outputDefaultCatalog,
      @Nullable Boolean outputDefaultSchema,
      boolean useOfflineConnection,
      @Nullable String version,
      @Nullable String catalog,
      @Nullable String schema,
      Map<String, String> databaseParams,
      String shortName,
      String productName,
      int majorVersion,
      int minorVersion,
      @Nullable String catalogName,
      @Nullable String schemaName)
      throws Exception {
    // given
    var builder =
        DatabaseBuilder.of()
            .withDatabaseFactory(toDatabaseFactory(databaseClass))
            .withObjectQuotingStrategy(quotingStrategy)
            .withOutputDefaultCatalog(outputDefaultCatalog)
            .withOutputDefaultSchema(outputDefaultSchema);
    if (useOfflineConnection) {
      builder =
          builder.withOfflineConnection(
              ocb ->
                  ocb.withVersion(version)
                      .withCatalog(catalog)
                      .withSchema(schema)
                      .withDatabaseParams(databaseParams));
    }

    // when
    var database = builder.build();

    // then
    assertThat(database.getClass()).isEqualTo(databaseClass);
    assertThat(database.getShortName()).isEqualTo(shortName);
    assertThat(database.getDatabaseProductName()).isEqualTo(productName);
    assertThat(database.getDatabaseProductVersion()).isEqualTo(version);
    assertThat(database.getDatabaseMajorVersion()).isEqualTo(majorVersion);
    assertThat(database.getDatabaseMinorVersion()).isEqualTo(minorVersion);
    assertThat(database.getDefaultCatalogName()).isEqualTo(catalogName);
    assertThat(database.getDefaultSchemaName()).isEqualTo(schemaName);
    assertThat(database.getObjectQuotingStrategy())
        .isEqualTo(Optional.ofNullable(quotingStrategy).orElse(ObjectQuotingStrategy.LEGACY));
    assertThat(database.getOutputDefaultCatalog())
        .isEqualTo(Optional.ofNullable(outputDefaultCatalog).orElse(true));
    assertThat(database.getOutputDefaultSchema())
        .isEqualTo(Optional.ofNullable(outputDefaultSchema).orElse(true));
    if (useOfflineConnection) {
      assertThat(database.getConnection()).isExactlyInstanceOf(MarkitectOfflineConnection.class);
    } else {
      assertThat(database.getConnection()).isNull();
    }

    // when
    builder = builder.withDatabaseFactory(MSSQLDatabase::new).withOfflineConnection();
    database = builder.build();

    // then
    assertThat(database.getClass()).isEqualTo(MSSQLDatabase.class);
    assertThat(database.getShortName()).isEqualTo("mssql");
    assertThat(database.getDatabaseProductName()).isEqualTo("Offline mssql");
    assertThat(database.getDatabaseProductVersion()).isNull();
    assertThat(database.getDatabaseMajorVersion()).isEqualTo(999);
    assertThat(database.getDatabaseMinorVersion()).isEqualTo(999);
    assertThat(database.getDefaultCatalogName()).isNull();
    assertThat(database.getDefaultSchemaName()).isNull();
    assertThat(database.getObjectQuotingStrategy())
        .isEqualTo(Optional.ofNullable(quotingStrategy).orElse(ObjectQuotingStrategy.LEGACY));
    assertThat(database.getOutputDefaultCatalog())
        .isEqualTo(Optional.ofNullable(outputDefaultCatalog).orElse(true));
    assertThat(database.getOutputDefaultSchema())
        .isEqualTo(Optional.ofNullable(outputDefaultSchema).orElse(true));
    assertThat(database.getConnection()).isExactlyInstanceOf(MarkitectOfflineConnection.class);
  }

  @Test
  void buildFailsOnDatabaseException() {
    // given
    var builder =
        DatabaseBuilder.of()
            .withDatabaseFactory(
                () -> {
                  throw new DatabaseException();
                });

    // when
    var thrown = catchThrowable(builder::build);

    // then
    assertThat(thrown).isInstanceOf(UncheckedDatabaseException.class);
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void buildWithInvalidDatabaseFactoryFails() {
    // given
    var invalidBuilder = DatabaseBuilder.of().withDatabaseFactory(() -> null);

    // when
    var thrown = catchThrowable(invalidBuilder::build);

    // then
    assertThat(thrown).isInstanceOf(VerifyException.class);
  }

  @Test
  void buildWithInvalidOfflineConnectionCustomizerFails() {
    // given
    var invalidBuilder =
        DatabaseBuilder.of()
            .withDatabaseFactory(H2Database::new)
            .withOfflineConnection(ocb -> null);

    // when
    var thrown = catchThrowable(invalidBuilder::build);

    // then
    assertThat(thrown).isInstanceOf(VerifyException.class);
  }

  @Test
  void buildWithoutDatabaseFactoryFails() {
    // given
    var invalidBuilder = DatabaseBuilder.of();

    // when
    var thrown = catchThrowable(invalidBuilder::build);

    // then
    assertThat(thrown).isInstanceOf(IllegalStateException.class);
  }
}
