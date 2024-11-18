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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.google.common.base.VerifyException;
import java.util.Map;
import java.util.Optional;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.H2Database;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        DatabaseBuilder.newBuilder(databaseClass)
            .objectQuotingStrategy(quotingStrategy)
            .outputDefaultCatalog(outputDefaultCatalog)
            .outputDefaultSchema(outputDefaultSchema);
    if (useOfflineConnection) {
      builder.offlineConnection(
          ocb ->
              ocb.version(version).catalog(catalog).schema(schema).databaseParams(databaseParams));
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
  }

  @ParameterizedTest
  @ValueSource(
      classes = {
        AbstractJdbcDatabase.class,
        Database.class,
        AbstractDb2Database.class,
        MarkitectDatabase.class,
      })
  void build_withInvalidDatabaseClass_throwsIllegalStateException(
      Class<? extends Database> databaseClass) {
    // given
    var invalidBuilder = DatabaseBuilder.newBuilder(databaseClass);

    // when
    var thrown = catchThrowable(invalidBuilder::build);

    // then
    assertThat(thrown).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void build_withInvalidOfflineConnectionCustomizer_throwsVerifyException() {
    // given
    var invalidBuilder =
        DatabaseBuilder.newBuilder(H2Database.class).offlineConnection(ocb -> null);

    // when
    var thrown = catchThrowable(invalidBuilder::build);

    // then
    assertThat(thrown).isInstanceOf(VerifyException.class);
  }
}
