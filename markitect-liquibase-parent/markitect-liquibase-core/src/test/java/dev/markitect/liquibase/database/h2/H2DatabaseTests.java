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

package dev.markitect.liquibase.database.h2;

import static org.assertj.core.api.Assertions.assertThat;

import dev.markitect.liquibase.base.Nullable;
import dev.markitect.liquibase.database.DatabaseBuilder;
import java.util.LinkedHashMap;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.H2Database;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class H2DatabaseTests {
  private final DatabaseBuilder<H2Database> databaseBuilder =
      DatabaseBuilder.of(H2Database.class).withOfflineConnection();

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # preserveSchemaCase | quotingStrategy   | objectName | objectType                      | expected
                               |                   |            | liquibase.structure.core.Table  |
                               |                   | TBL1       | liquibase.structure.core.Table  | TBL1
                               | QUOTE_ALL_OBJECTS | TBL1       | liquibase.structure.core.Table  | TBL1
                               |                   | Tbl1       | liquibase.structure.core.Table  | TBL1
                               | QUOTE_ALL_OBJECTS | Tbl1       | liquibase.structure.core.Table  | Tbl1
                               |                   | Tbl 1      | liquibase.structure.core.Table  | TBL 1
                               | QUOTE_ALL_OBJECTS | Tbl 1      | liquibase.structure.core.Table  | Tbl 1
                               |                   | SCH1       | liquibase.structure.core.Schema | SCH1
                               | QUOTE_ALL_OBJECTS | SCH1       | liquibase.structure.core.Schema | SCH1
          true                 |                   | SCH1       | liquibase.structure.core.Schema | SCH1
                               |                   | Sch1       | liquibase.structure.core.Schema | SCH1
                               | QUOTE_ALL_OBJECTS | Sch1       | liquibase.structure.core.Schema | Sch1
          true                 |                   | Sch1       | liquibase.structure.core.Schema | Sch1
                               |                   | Sch 1      | liquibase.structure.core.Schema | SCH 1
                               | QUOTE_ALL_OBJECTS | Sch 1      | liquibase.structure.core.Schema | Sch 1
          true                 |                   | Sch 1      | liquibase.structure.core.Schema | Sch 1
          """,
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
    try (var database = databaseBuilder.withObjectQuotingStrategy(quotingStrategy).build()) {

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
          # preserveSchemaCase | quotingStrategy   | objectName | objectType                      | expected
                               |                   |            | liquibase.structure.core.Table  |
                               |                   | TBL1       | liquibase.structure.core.Table  | TBL1
                               | QUOTE_ALL_OBJECTS | TBL1       | liquibase.structure.core.Table  | "TBL1"
                               |                   | Tbl1       | liquibase.structure.core.Table  | Tbl1
                               | QUOTE_ALL_OBJECTS | Tbl1       | liquibase.structure.core.Table  | "Tbl1"
                               |                   | Tbl 1      | liquibase.structure.core.Table  | "Tbl 1"
                               | QUOTE_ALL_OBJECTS | Tbl 1      | liquibase.structure.core.Table  | "Tbl 1"
                               |                   | SCH1       | liquibase.structure.core.Schema | SCH1
                               | QUOTE_ALL_OBJECTS | SCH1       | liquibase.structure.core.Schema | "SCH1"
          true                 |                   | SCH1       | liquibase.structure.core.Schema | "SCH1"
                               |                   | Sch1       | liquibase.structure.core.Schema | Sch1
                               | QUOTE_ALL_OBJECTS | Sch1       | liquibase.structure.core.Schema | "Sch1"
          true                 |                   | Sch1       | liquibase.structure.core.Schema | "Sch1"
                               |                   | Sch 1      | liquibase.structure.core.Schema | "Sch 1"
                               | QUOTE_ALL_OBJECTS | Sch 1      | liquibase.structure.core.Schema | "Sch 1"
          true                 |                   | Sch 1      | liquibase.structure.core.Schema | "Sch 1"
          """,
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
    try (var database = databaseBuilder.withObjectQuotingStrategy(quotingStrategy).build()) {

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
          # outputDefaultSchema | catalogName | schemaName | tableName | expected
                                |             |            | Tbl1      | PUBLIC.Tbl1
                                |             | PUBLIC     | Tbl1      | PUBLIC.Tbl1
          false                 |             |            | Tbl1      | Tbl1
          false                 |             | PUBLIC     | Tbl1      | Tbl1
          false                 |             | lbschem2   | Tbl1      | lbschem2.Tbl1
          """,
      delimiter = '|')
  void escapeTableName(
      @Nullable Boolean outputDefaultSchema,
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String tableName,
      @Nullable String expected)
      throws Exception {
    // given
    try (var database = databaseBuilder.withOutputDefaultSchema(outputDefaultSchema).build()) {
      assertThat(database.getDefaultSchemaName()).isEqualTo("PUBLIC");

      // when
      String actual = database.escapeTableName(catalogName, schemaName, tableName);

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }
}
