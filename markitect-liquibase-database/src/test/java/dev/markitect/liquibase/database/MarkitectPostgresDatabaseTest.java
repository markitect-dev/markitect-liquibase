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

import java.util.LinkedHashMap;
import java.util.Map;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MarkitectPostgresDatabaseTest {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          preserveSchemaCase | quotingStrategy   | objectName | objectType | expected
                             |                   |            | Table      |
                             |                   | Tbl1       | Table      | Tbl1
                             | QUOTE_ALL_OBJECTS | Tbl1       | Table      | "Tbl1"
                             |                   | Tbl 1      | Table      | "tbl 1"
                             | QUOTE_ALL_OBJECTS | Tbl 1      | Table      | "Tbl 1"
                             |                   | Sch1       | Schema     | Sch1
                             | QUOTE_ALL_OBJECTS | Sch1       | Schema     | "Sch1"
          true               |                   | Sch1       | Schema     | "Sch1"
                             |                   | Sch 1      | Schema     | "sch 1"
                             | QUOTE_ALL_OBJECTS | Sch 1      | Schema     | "Sch 1"
          true               |                   | Sch 1      | Schema     | "Sch 1"
          """,
      useHeadersInDisplayName = true,
      delimiter = '|')
  void escapeObjectName(
      Boolean preserveSchemaCase,
      ObjectQuotingStrategy quotingStrategy,
      String objectName,
      @ToObjectType Class<? extends DatabaseObject> objectType,
      String expected)
      throws Exception {
    // given
    Map<String, Object> scopeValues = new LinkedHashMap<>();
    if (preserveSchemaCase != null) {
      scopeValues.put(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey(), preserveSchemaCase);
    }
    var database =
        DatabaseBuilder.of(MarkitectPostgresDatabase::new)
            .setResourceAccessor(new ClassLoaderResourceAccessor())
            .setObjectQuotingStrategy(quotingStrategy)
            .build();

    // when
    String actual =
        Scope.child(scopeValues, () -> database.escapeObjectName(objectName, objectType));

    // then
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          preserveSchemaCase | quotingStrategy   | objectName | objectType | expected
                             |                   |            | Table      |
                             |                   | Tbl1       | Table      | tbl1
                             | QUOTE_ALL_OBJECTS | Tbl1       | Table      | Tbl1
                             |                   | Tbl 1      | Table      | tbl 1
                             | QUOTE_ALL_OBJECTS | Tbl 1      | Table      | Tbl 1
                             |                   | Sch1       | Schema     | sch1
                             | QUOTE_ALL_OBJECTS | Sch1       | Schema     | Sch1
          true               |                   | Sch1       | Schema     | Sch1
                             |                   | Sch 1      | Schema     | sch 1
                             | QUOTE_ALL_OBJECTS | Sch 1      | Schema     | Sch 1
          true               |                   | Sch 1      | Schema     | Sch 1
          """,
      useHeadersInDisplayName = true,
      delimiter = '|')
  void correctObjectName(
      Boolean preserveSchemaCase,
      ObjectQuotingStrategy quotingStrategy,
      String objectName,
      @ToObjectType Class<? extends DatabaseObject> objectType,
      String expected)
      throws Exception {
    // given
    Map<String, Object> scopeValues = new LinkedHashMap<>();
    if (preserveSchemaCase != null) {
      scopeValues.put(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey(), preserveSchemaCase);
    }
    var database =
        DatabaseBuilder.of(MarkitectPostgresDatabase::new)
            .setResourceAccessor(new ClassLoaderResourceAccessor())
            .setObjectQuotingStrategy(quotingStrategy)
            .build();

    // when
    String actual =
        Scope.child(scopeValues, () -> database.correctObjectName(objectName, objectType));

    // then
    assertThat(actual).isEqualTo(expected);
  }
}
