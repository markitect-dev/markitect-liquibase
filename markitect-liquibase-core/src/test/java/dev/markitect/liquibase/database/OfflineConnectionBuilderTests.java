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

import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.jupiter.json.JsonSource;

class OfflineConnectionBuilderTests {
  @ParameterizedTest
  @JsonSource(
      """
      [
        {
          shortName: 'h2',
          databaseParams: {},
          expectedProductName: 'Offline h2',
          majorVersion: 999,
          minorVersion: 999
        },
        {
          shortName: 'h2',
          version: '1.4.200',
          databaseParams: {},
          expectedProductName: 'Offline h2',
          productVersion: '1.4.200',
          majorVersion: 1,
          minorVersion: 4
        },
        {
          shortName: 'mssql',
          databaseParams: {},
          expectedProductName: 'Offline mssql',
          majorVersion: 999,
          minorVersion: 999
        },
        {
          shortName: 'mssql',
          snapshot: 'snapshots/mssql/AdventureWorks2022.json',
          databaseParams: {},
          expectedProductName: 'Offline mssql',
          productVersion: '16.00.4025',
          majorVersion: 999,
          minorVersion: 999,
          expectedCatalog: 'AdventureWorks2022',
          expectedSchema: 'dbo'
        },
        {
          shortName: 'mssql',
          productName: 'Microsoft SQL Server',
          version: '16.00.4025',
          snapshot: 'snapshots/mssql/AdventureWorks2022.json',
          catalog: 'Cat1',
          schema: 'Sch1',
          databaseParams: {},
          expectedProductName: 'Microsoft SQL Server',
          productVersion: '16.00.4025',
          majorVersion: 16,
          minorVersion: 0,
          expectedCatalog: 'Cat1',
          expectedSchema: 'Sch1'
        },
        {
          shortName: 'mssql',
          databaseParams: {
            defaultCatalogName: 'Cat1'
          },
          expectedProductName: 'Offline mssql',
          majorVersion: 999,
          minorVersion: 999
        },
        {
          shortName: 'postgresql',
          databaseParams: {},
          expectedProductName: 'Offline postgresql',
          majorVersion: 999,
          minorVersion: 999
        }
      ]
      """)
  void build(
      String shortName,
      @Nullable String productName,
      @Nullable String version,
      @Nullable String snapshot,
      @Nullable String catalog,
      @Nullable String schema,
      Map<String, String> databaseParams,
      String expectedProductName,
      String productVersion,
      int majorVersion,
      int minorVersion,
      @Nullable String expectedCatalog,
      @Nullable String expectedSchema)
      throws Exception {
    // given
    var builder =
        OfflineConnectionBuilder.of()
            .withShortName(shortName)
            .withProductName(productName)
            .withVersion(version)
            .withSnapshot(snapshot)
            .withCatalog(catalog)
            .withSchema(schema)
            .withDatabaseParams(databaseParams);

    // when
    var connection = builder.build();

    // then
    assertThat(connection.getDatabaseProductName()).isEqualTo(expectedProductName);
    assertThat(connection.getDatabaseProductVersion()).isEqualTo(productVersion);
    assertThat(connection.getDatabaseMajorVersion()).isEqualTo(majorVersion);
    assertThat(connection.getDatabaseMinorVersion()).isEqualTo(minorVersion);
    if (snapshot == null) {
      assertThat(connection).extracting("snapshot").isNull();
    } else {
      assertThat(connection).extracting("snapshot").isNotNull();
    }
    assertThat(connection.getCatalog()).isEqualTo(expectedCatalog);
    assertThat(connection.getSchema()).isEqualTo(expectedSchema);
    assertThat(connection).extracting("databaseParams").isEqualTo(databaseParams);
  }

  @Test
  void build_withoutShortName_throwsIllegalStateException() {
    // given
    var builder = OfflineConnectionBuilder.of();

    // when
    var thrown = catchThrowable(builder::build);

    // then
    assertThat(thrown).isInstanceOf(IllegalStateException.class);
  }
}
