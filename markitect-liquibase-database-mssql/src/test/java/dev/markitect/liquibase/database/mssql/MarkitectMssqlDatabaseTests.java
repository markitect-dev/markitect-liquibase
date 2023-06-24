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

package dev.markitect.liquibase.database.mssql;

import static org.assertj.core.api.Assertions.assertThat;

import dev.markitect.liquibase.base.Nullable;
import dev.markitect.liquibase.database.DatabaseBuilder;
import java.util.LinkedHashMap;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MarkitectMssqlDatabaseTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          preserveSchemaCase | quotingStrategy   | objectName | objectType                      | expected
                             |                   |            | liquibase.structure.core.Table  |
                             |                   | Tbl1       | liquibase.structure.core.Table  | Tbl1
                             | QUOTE_ALL_OBJECTS | Tbl1       | liquibase.structure.core.Table  | Tbl1
                             |                   | Sch1       | liquibase.structure.core.Schema | Sch1
          true               |                   | Sch1       | liquibase.structure.core.Schema | Sch1
                             |                   | Tbl 1      | liquibase.structure.core.Table  | Tbl 1
                             | QUOTE_ALL_OBJECTS | Tbl 1      | liquibase.structure.core.Table  | Tbl 1
                             |                   | Sch 1      | liquibase.structure.core.Schema | Sch 1
          true               |                   | Sch 1      | liquibase.structure.core.Schema | Sch 1
          """,
      useHeadersInDisplayName = true,
      delimiter = '|')
  void correctObjectName(
      @Nullable Boolean preserveSchemaCase,
      @Nullable ObjectQuotingStrategy quotingStrategy,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType,
      @Nullable String expected)
      throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    if (preserveSchemaCase != null) {
      scopeValues.put(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey(), preserveSchemaCase);
    }
    try (var database =
        DatabaseBuilder.of(MarkitectMssqlDatabase::new)
            .setResourceAccessor(new ClassLoaderResourceAccessor())
            .useOfflineConnection()
            .setObjectQuotingStrategy(quotingStrategy)
            .build()) {

      // when
      String actual =
          Scope.child(scopeValues, () -> database.correctObjectName(objectName, objectType));

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          includeCatalog | outputDefaultCatalog | outputDefaultSchema | catalogName | schemaName | objectName | objectType                     | expected
                         |                      |                     |             |            | Idx1       | liquibase.structure.core.Index | Idx1
                         |                      |                     |             | Sch1       | Idx1       | liquibase.structure.core.Index | Idx1
          """,
      useHeadersInDisplayName = true,
      delimiter = '|')
  void escapeObjectName_catalogName_schemaName_objectName_objectType(
      @Nullable Boolean includeCatalog,
      @Nullable Boolean outputDefaultCatalog,
      @Nullable Boolean outputDefaultSchema,
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType,
      @Nullable String expected)
      throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    if (includeCatalog != null) {
      scopeValues.put(
          GlobalConfiguration.INCLUDE_CATALOG_IN_SPECIFICATION.getKey(), includeCatalog);
    }
    try (var database =
        DatabaseBuilder.of(MarkitectMssqlDatabase::new)
            .setResourceAccessor(new ClassLoaderResourceAccessor())
            .setOutputDefaultCatalog(outputDefaultCatalog)
            .setOutputDefaultSchema(outputDefaultSchema)
            .useOfflineConnection(ocb -> ocb.setCatalog("Cat1").setSchema("Sch1"))
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo("Cat1");
      assertThat(database.getDefaultSchemaName()).isEqualTo("Sch1");

      // when
      String actual =
          Scope.child(
              scopeValues,
              () -> database.escapeObjectName(catalogName, schemaName, objectName, objectType));

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          preserveSchemaCase | quotingStrategy   | objectName | objectType                      | expected
                             |                   |            | liquibase.structure.core.Table  |
                             |                   | Tbl1       | liquibase.structure.core.Table  | Tbl1
                             | QUOTE_ALL_OBJECTS | Tbl1       | liquibase.structure.core.Table  | [Tbl1]
                             |                   | Sch1       | liquibase.structure.core.Schema | Sch1
          true               |                   | Sch1       | liquibase.structure.core.Schema | Sch1
                             |                   | Tbl 1      | liquibase.structure.core.Table  | [Tbl 1]
                             | QUOTE_ALL_OBJECTS | Tbl 1      | liquibase.structure.core.Table  | [Tbl 1]
                             |                   | Sch 1      | liquibase.structure.core.Schema | [Sch 1]
          true               |                   | Sch 1      | liquibase.structure.core.Schema | [Sch 1]
          """,
      useHeadersInDisplayName = true,
      delimiter = '|')
  void escapeObjectName(
      @Nullable Boolean preserveSchemaCase,
      @Nullable ObjectQuotingStrategy quotingStrategy,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType,
      @Nullable String expected)
      throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    if (preserveSchemaCase != null) {
      scopeValues.put(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey(), preserveSchemaCase);
    }
    try (var database =
        DatabaseBuilder.of(MarkitectMssqlDatabase::new)
            .setResourceAccessor(new ClassLoaderResourceAccessor())
            .useOfflineConnection()
            .setObjectQuotingStrategy(quotingStrategy)
            .build()) {

      // when
      String actual =
          Scope.child(scopeValues, () -> database.escapeObjectName(objectName, objectType));

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          includeCatalog | outputDefaultCatalog | outputDefaultSchema | catalogName | schemaName | tableName | expected
                         |                      |                     |             |            | Tbl1      | Sch1.Tbl1
                         |                      |                     |             | Sch1       | Tbl1      | Sch1.Tbl1
                         |                      | false               |             |            | Tbl1      | Tbl1
                         |                      | false               |             | Sch1       | Tbl1      | Tbl1
                         |                      | false               |             | Sch2       | Tbl1      | Sch2.Tbl1
          true           |                      |                     |             |            | Tbl1      | Cat1.Sch1.Tbl1
          true           |                      |                     |             | Sch1       | Tbl1      | Cat1.Sch1.Tbl1
          true           |                      | false               |             |            | Tbl1      | Cat1..Tbl1
          true           |                      | false               |             | Sch1       | Tbl1      | Cat1..Tbl1
          true           | false                | false               |             |            | Tbl1      | Tbl1
          true           | false                | false               |             | Sch1       | Tbl1      | Tbl1
                         | false                |                     | Cat2        |            | Tbl1      | Cat2..Tbl1
                         | false                |                     | Cat2        | Sch1       | Tbl1      | Cat2.Sch1.Tbl1
          """,
      useHeadersInDisplayName = true,
      delimiter = '|')
  void escapeTableName(
      @Nullable Boolean includeCatalog,
      @Nullable Boolean outputDefaultCatalog,
      @Nullable Boolean outputDefaultSchema,
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String tableName,
      @Nullable String expected)
      throws Exception {
    var scopeValues = new LinkedHashMap<String, Object>();
    if (includeCatalog != null) {
      scopeValues.put(
          GlobalConfiguration.INCLUDE_CATALOG_IN_SPECIFICATION.getKey(), includeCatalog);
    }
    try (var database =
        DatabaseBuilder.of(MarkitectMssqlDatabase::new)
            .setResourceAccessor(new ClassLoaderResourceAccessor())
            .setOutputDefaultCatalog(outputDefaultCatalog)
            .setOutputDefaultSchema(outputDefaultSchema)
            .useOfflineConnection(ocb -> ocb.setCatalog("Cat1").setSchema("Sch1"))
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo("Cat1");
      assertThat(database.getDefaultSchemaName()).isEqualTo("Sch1");

      // when
      String actual =
          Scope.child(
              scopeValues, () -> database.escapeTableName(catalogName, schemaName, tableName));

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }
}
