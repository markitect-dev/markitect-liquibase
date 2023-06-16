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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Map;
import java.util.stream.Stream;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.json.JsonSource;
import org.junitpioneer.jupiter.json.Property;

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
          snapshot: 'snapshots/snapshot-mssql.json',
          databaseParams: {},
          expectedProductName: 'Offline mssql',
          productVersion: '16.00.4025',
          majorVersion: 999,
          minorVersion: 999,
          expectedCatalog: 'AdventureWorks2019',
          expectedSchema: 'dbo'
        },
        {
          shortName: 'mssql',
          productName: 'Microsoft SQL Server',
          version: '16.00.4025',
          snapshot: 'snapshots/snapshot-mssql.json',
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
      @Property("shortName") String shortName,
      @Property("productName") String productName,
      @Property("version") String version,
      @Property("snapshot") String snapshot,
      @Property("catalog") String catalog,
      @Property("schema") String schema,
      @Property("databaseParams") Map<String, String> databaseParams,
      @Property("expectedProductName") String expectedProductName,
      @Property("productVersion") String productVersion,
      @Property("majorVersion") int majorVersion,
      @Property("minorVersion") int minorVersion,
      @Property("expectedCatalog") String expectedCatalog,
      @Property("expectedSchema") String expectedSchema)
      throws Exception {
    // given
    var builder =
        OfflineConnectionBuilder.of()
            .setResourceAccessor(new ClassLoaderResourceAccessor())
            .setShortName(shortName)
            .setProductName(productName)
            .setVersion(version)
            .setSnapshot(snapshot)
            .setCatalog(catalog)
            .setSchema(schema)
            .setDatabaseParams(databaseParams);

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

  static Stream<Arguments> buildFails() {
    return Stream.of(arguments(new ClassLoaderResourceAccessor(), null), arguments(null, "h2"));
  }

  @ParameterizedTest
  @MethodSource
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void buildFails(ResourceAccessor resourceAccessor, String shortName) {
    // given
    var builder =
        OfflineConnectionBuilder.of().setResourceAccessor(resourceAccessor).setShortName(shortName);

    // when
    var thrown = catchThrowable(builder::build);

    // then
    assertThat(thrown).isInstanceOf(IllegalStateException.class);
  }
}
