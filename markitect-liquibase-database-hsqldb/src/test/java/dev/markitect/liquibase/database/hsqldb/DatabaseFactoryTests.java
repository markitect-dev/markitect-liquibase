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

package dev.markitect.liquibase.database.hsqldb;

import static org.assertj.core.api.Assertions.assertThat;

import dev.markitect.liquibase.database.OfflineConnectionBuilder;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DatabaseFactoryTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          shortName | expectedType
          hsqldb    | dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase
          """,
      useHeadersInDisplayName = true,
      delimiter = '|')
  void findCorrectDatabaseImplementation(String shortName, Class<? extends Database> expectedType)
      throws Exception {
    // given
    var connection = OfflineConnectionBuilder.of().withShortName(shortName).build();

    // when
    var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

    // then
    assertThat(database)
        .extracting(Database::getShortName, Object::getClass)
        .containsExactly(shortName, expectedType);
  }
}
