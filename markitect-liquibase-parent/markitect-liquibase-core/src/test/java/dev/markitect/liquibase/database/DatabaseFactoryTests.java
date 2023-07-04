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

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DatabaseFactoryTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # shortName | databaseClass
          h2          | dev.markitect.liquibase.database.h2.MarkitectH2Database
          hsqldb      | dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase
          mariadb     | liquibase.database.core.MariaDBDatabase
          mssql       | dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase
          mysql       | liquibase.database.core.MySQLDatabase
          oracle      | liquibase.database.core.OracleDatabase
          postgresql  | dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase
          """,
      delimiter = '|')
  void findCorrectDatabaseImplementation(String shortName, Class<? extends Database> databaseClass)
      throws Exception {
    // given
    var connection = OfflineConnectionBuilder.of().withShortName(shortName).build();

    // when
    var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

    // then
    assertThat(database)
        .extracting(Database::getShortName, Object::getClass)
        .containsExactly(shortName, databaseClass);
  }
}
