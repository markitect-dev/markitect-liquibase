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

package dev.markitect.liquibase.change;

import static org.assertj.core.api.Assertions.assertThat;

import dev.markitect.liquibase.database.DatabaseBuilder;
import liquibase.change.core.LoadDataChange.LOAD_DATA_TYPE;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MarkitectLoadDataChangeTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                          | expected
          liquibase.database.core.H2Database       | true
          liquibase.database.core.HsqlDatabase     | true
          liquibase.database.core.MSSQLDatabase    | true
          liquibase.database.core.OracleDatabase   | false
          liquibase.database.core.PostgresDatabase | true
          """,
      delimiter = '|')
  void supports(Class<? extends Database> databaseClass, boolean expected) throws Exception {
    // given
    var change = new MarkitectLoadDataChange();
    try (var database = DatabaseBuilder.of(databaseClass).build()) {

      // when
      var actual = change.supports(database);

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # columnName | columnType | expected
                       |            | false
          name         |            | true
          name         | STRING     | false
          """,
      delimiter = '|')
  void generateStatementsVolatile(
      @Nullable String columnName, @Nullable LOAD_DATA_TYPE columnType, boolean expected)
      throws Exception {
    // given
    var change = new MarkitectLoadDataChange();
    if (columnName != null) {
      var column = new LoadDataColumnConfig();
      column.setName(columnName);
      if (columnType != null) {
        column.setType(columnType);
      }
      change.addColumn(column);
    }
    try (var database = DatabaseBuilder.of(H2Database.class).build()) {

      // when
      var actual = change.generateStatementsVolatile(database);

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }
}
