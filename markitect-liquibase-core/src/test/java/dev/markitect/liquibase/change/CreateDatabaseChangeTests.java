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
import dev.markitect.liquibase.statement.CreateDatabaseStatement;
import java.util.List;
import liquibase.database.Database;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.structure.core.Catalog;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.json.JsonSource;

class CreateDatabaseChangeTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                          | expected
          liquibase.database.core.H2Database       | false
          liquibase.database.core.HsqlDatabase     | false
          liquibase.database.core.MSSQLDatabase    | true
          liquibase.database.core.OracleDatabase   | false
          liquibase.database.core.PostgresDatabase | true
          """,
      delimiter = '|')
  void supports(Class<? extends Database> databaseClass, boolean expected) throws Exception {
    // given
    var change = new CreateDatabaseChange();
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {

      // when
      var supports = change.supports(database);

      // then
      assertThat(supports).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @JsonSource(
      """
      [
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          expectedMessages: []
        },
        {
          databaseClass: 'liquibase.database.core.PostgresDatabase',
          expectedMessages: []
        }
      ]
      """)
  void warn(Class<? extends Database> databaseClass, List<String> expectedMessages)
      throws Exception {
    // given
    var change = new CreateDatabaseChange();
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {
      assertThat(change.supports(database)).isTrue();

      // when
      var warnings = change.warn(database);

      // then
      assertThat(warnings.getMessages()).containsExactlyInAnyOrderElementsOf(expectedMessages);
      assertThat(warnings.hasWarnings()).isEqualTo(!expectedMessages.isEmpty());
    }
  }

  @ParameterizedTest
  @JsonSource(
      """
      [
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          expectedErrorMessages: [
            'databaseName is required for createDatabase on mssql'
          ]
        },
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          databaseName: 'cat1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'liquibase.database.core.PostgresDatabase',
          databaseName: 'cat1',
          expectedErrorMessages: []
        }
      ]
      """)
  void validate(
      Class<? extends Database> databaseClass,
      String databaseName,
      List<String> expectedErrorMessages)
      throws Exception {
    // given
    var change = new CreateDatabaseChange();
    change.setDatabaseName(databaseName);
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {
      assertThat(change.supports(database)).isTrue();

      // when
      var errors = change.validate(database);

      // then
      assertThat(errors.getErrorMessages())
          .containsExactlyInAnyOrderElementsOf(expectedErrorMessages);
      assertThat(errors.hasErrors()).isEqualTo(!expectedErrorMessages.isEmpty());
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseName
          cat1
          """,
      delimiter = '|')
  void createInverses(String databaseName) {
    // given
    var change = new CreateDatabaseChange();
    change.setDatabaseName(databaseName);
    var inverse = new DropDatabaseChange();
    inverse.setDatabaseName(databaseName);

    // when
    var inverses = change.createInverses();

    // then
    assertThat(inverses).usingRecursiveFieldByFieldElementComparator().containsExactly(inverse);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseName | expected
          cat1           | Database cat1 created
          """,
      delimiter = '|')
  void getConfirmationMessage(String databaseName, String expected) {
    // given
    var change = new CreateDatabaseChange();
    change.setDatabaseName(databaseName);

    // when
    String message = change.getConfirmationMessage();

    // then
    assertThat(message).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                          | databaseName
          liquibase.database.core.MSSQLDatabase    | cat1
          liquibase.database.core.PostgresDatabase | cat1
          """,
      delimiter = '|')
  void generateStatements(Class<? extends Database> databaseClass, String databaseName)
      throws Exception {
    // given
    var change = new CreateDatabaseChange();
    change.setDatabaseName(databaseName);
    var statement = new CreateDatabaseStatement();
    statement.setDatabaseName(databaseName);
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {
      assertThat(change.supports(database)).isTrue();
      assertThat(change.warn(database).hasWarnings()).isFalse();
      assertThat(change.validate(database).hasErrors()).isFalse();

      // when
      var statements = change.generateStatements(database);

      // then
      assertThat(statements)
          .usingRecursiveFieldByFieldElementComparator()
          .containsExactly(statement);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                          | databaseName | expectedSql
          liquibase.database.core.MSSQLDatabase    | cat1         | CREATE DATABASE cat1
          liquibase.database.core.PostgresDatabase | cat1         | CREATE DATABASE cat1
          """,
      delimiter = '|')
  void generateSql(Class<? extends Database> databaseClass, String databaseName, String expectedSql)
      throws Exception {
    // given
    var change = new CreateDatabaseChange();
    change.setDatabaseName(databaseName);
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {
      assertThat(change.supports(database)).isTrue();
      assertThat(change.warn(database).hasWarnings()).isFalse();
      assertThat(change.validate(database).hasErrors()).isFalse();

      // when
      var sql = SqlGeneratorFactory.getInstance().generateSql(change, database);

      // then
      assertThat(sql)
          .usingRecursiveFieldByFieldElementComparator()
          .containsExactly(new UnparsedSql(expectedSql, new Catalog(databaseName)));
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                          | databaseName | expectedSql
          liquibase.database.core.MSSQLDatabase    | cat1         | DROP DATABASE cat1
          liquibase.database.core.PostgresDatabase | cat1         | DROP DATABASE cat1
          """,
      delimiter = '|')
  void generateRollbackSql(
      Class<? extends Database> databaseClass, String databaseName, String expectedSql)
      throws Exception {
    // given
    var change = new CreateDatabaseChange();
    change.setDatabaseName(databaseName);
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {
      assertThat(change.supports(database)).isTrue();
      assertThat(change.warn(database).hasWarnings()).isFalse();
      assertThat(change.validate(database).hasErrors()).isFalse();

      // when
      var rollbackSql =
          SqlGeneratorFactory.getInstance()
              .generateSql(change.generateRollbackStatements(database), database);

      // then
      assertThat(rollbackSql)
          .usingRecursiveFieldByFieldElementComparator()
          .containsExactly(new UnparsedSql(expectedSql, new Catalog(databaseName)));
    }
  }
}
