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

package dev.markitect.liquibase.change;

import static org.assertj.core.api.Assertions.assertThat;

import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.statement.CreateSchemaStatement;
import java.util.List;
import liquibase.database.Database;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.structure.core.Schema;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.json.JsonSource;

class CreateSchemaChangeTests {
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
    var change = new CreateSchemaChange();
    try (var database = DatabaseBuilder.of(databaseClass).build()) {

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
          databaseClass: 'liquibase.database.core.H2Database',
          expectedMessages: []
        },
        {
          databaseClass: 'liquibase.database.core.HsqlDatabase',
          expectedMessages: []
        },
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
    var change = new CreateSchemaChange();
    try (var database = DatabaseBuilder.of(databaseClass).build()) {
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
          databaseClass: 'liquibase.database.core.H2Database',
          expectedErrorMessages: [
            'schemaName is required for createSchema on h2'
          ]
        },
        { databaseClass: 'liquibase.database.core.H2Database',
          schemaName: 'sch1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'liquibase.database.core.H2Database',
          catalogName: 'cat1',
          expectedErrorMessages: [
            'schemaName is required for createSchema on h2'
          ]
        },
        {
          databaseClass: 'liquibase.database.core.H2Database',
          catalogName: 'cat1',
          schemaName: 'sch1',
          expectedErrorMessages: [
            'catalogName is not allowed on h2'
          ]
        },
        {
          databaseClass: 'liquibase.database.core.HsqlDatabase',
          schemaName: 'sch1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          schemaName: 'sch1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          catalogName: 'cat1',
          schemaName: 'sch1',
          expectedErrorMessages: [
            'catalogName is not allowed on mssql'
          ]
        },
        {
          databaseClass: 'liquibase.database.core.PostgresDatabase',
          schemaName: 'sch1',
          expectedErrorMessages: []
        }
      ]
      """)
  void validate(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      String schemaName,
      List<String> expectedErrorMessages)
      throws Exception {
    // given
    var change = new CreateSchemaChange();
    change.setCatalogName(catalogName);
    change.setSchemaName(schemaName);
    try (var database = DatabaseBuilder.of(databaseClass).build()) {
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
          # catalogName | schemaName
                        | sch1
          cat1          | sch1
          """,
      delimiter = '|')
  void createInverses(@Nullable String catalogName, String schemaName) {
    // given
    var change = new CreateSchemaChange();
    change.setCatalogName(catalogName);
    change.setSchemaName(schemaName);
    var inverse = new DropSchemaChange();
    inverse.setCatalogName(catalogName);
    inverse.setSchemaName(schemaName);

    // when
    var inverses = change.createInverses();

    // then
    assertThat(inverses).usingRecursiveFieldByFieldElementComparator().containsExactly(inverse);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # catalogName | schemaName | expected
                        | sch1       | Schema sch1 created
          cat1          | sch1       | Schema cat1.sch1 created
          """,
      delimiter = '|')
  void getConfirmationMessage(@Nullable String catalogName, String schemaName, String expected) {
    // given
    var change = new CreateSchemaChange();
    change.setCatalogName(catalogName);
    change.setSchemaName(schemaName);

    // when
    String message = change.getConfirmationMessage();

    // then
    assertThat(message).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                          | catalogName | schemaName
          liquibase.database.core.H2Database       |             | sch1
          liquibase.database.core.HsqlDatabase     |             | sch1
          liquibase.database.core.MSSQLDatabase    |             | sch1
          liquibase.database.core.PostgresDatabase |             | sch1
          """,
      delimiter = '|')
  void generateStatements(
      Class<? extends Database> databaseClass, @Nullable String catalogName, String schemaName)
      throws Exception {
    // given
    var change = new CreateSchemaChange();
    change.setCatalogName(catalogName);
    change.setSchemaName(schemaName);
    var statement = new CreateSchemaStatement();
    statement.setCatalogName(catalogName);
    statement.setSchemaName(schemaName);
    try (var database = DatabaseBuilder.of(databaseClass).build()) {
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
          # databaseClass                          | catalogName | schemaName | expectedSql
          liquibase.database.core.H2Database       |             | sch1       | CREATE SCHEMA sch1
          liquibase.database.core.HsqlDatabase     |             | sch1       | CREATE SCHEMA sch1
          liquibase.database.core.MSSQLDatabase    |             | sch1       | CREATE SCHEMA sch1
          liquibase.database.core.PostgresDatabase |             | sch1       | CREATE SCHEMA sch1
          """,
      delimiter = '|')
  void generateSql(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      String schemaName,
      String expectedSql)
      throws Exception {
    // given
    var change = new CreateSchemaChange();
    change.setCatalogName(catalogName);
    change.setSchemaName(schemaName);
    try (var database = DatabaseBuilder.of(databaseClass).build()) {
      assertThat(change.supports(database)).isTrue();
      assertThat(change.warn(database).hasWarnings()).isFalse();
      assertThat(change.validate(database).hasErrors()).isFalse();

      // when
      var sql = SqlGeneratorFactory.getInstance().generateSql(change, database);

      // then
      assertThat(sql)
          .usingRecursiveFieldByFieldElementComparator()
          .containsExactly(new UnparsedSql(expectedSql, new Schema(catalogName, schemaName)));
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                          | catalogName | schemaName | expectedSql
          liquibase.database.core.H2Database       |             | sch1       | DROP SCHEMA sch1
          liquibase.database.core.HsqlDatabase     |             | sch1       | DROP SCHEMA sch1
          liquibase.database.core.MSSQLDatabase    |             | sch1       | DROP SCHEMA sch1
          liquibase.database.core.PostgresDatabase |             | sch1       | DROP SCHEMA sch1
          """,
      delimiter = '|')
  void generateRollbackSql(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      String schemaName,
      String expectedSql)
      throws Exception {
    // given
    var change = new CreateSchemaChange();
    change.setCatalogName(catalogName);
    change.setSchemaName(schemaName);
    try (var database = DatabaseBuilder.of(databaseClass).build()) {
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
          .containsExactly(new UnparsedSql(expectedSql, new Schema(catalogName, schemaName)));
    }
  }
}
