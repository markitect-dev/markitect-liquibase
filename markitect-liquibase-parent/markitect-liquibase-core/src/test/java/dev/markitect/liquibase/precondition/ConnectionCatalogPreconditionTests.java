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

package dev.markitect.liquibase.precondition;

import static liquibase.serializer.LiquibaseSerializable.GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase;
import java.util.List;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.FailedPrecondition;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.json.JsonSource;

class ConnectionCatalogPreconditionTests {
  @Test
  void test() {
    // when
    var precondition = new ConnectionCatalogPrecondition();
    precondition.setCatalogName("cat1");

    // then
    assertThat(precondition.getCatalogName()).isEqualTo("cat1");
    assertThat(precondition.getSerializedObjectNamespace())
        .isEqualTo(GENERIC_CHANGELOG_EXTENSION_NAMESPACE);
    assertThat(precondition.getSerializedObjectName()).isEqualTo("connectionCatalog");
  }

  @ParameterizedTest
  @JsonSource(
      """
      [
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          expectedMessages: []
        }
      ]
      """)
  void warn(Class<? extends Database> databaseClass, List<String> expectedMessages)
      throws Exception {
    // given
    var precondition = new ConnectionCatalogPrecondition();
    try (var database = DatabaseBuilder.of(databaseClass).build()) {

      // when
      var warnings = precondition.warn(database);

      // then
      assertThat(warnings.hasWarnings()).isEqualTo(!expectedMessages.isEmpty());
      assertThat(warnings.getMessages()).containsExactlyInAnyOrderElementsOf(expectedMessages);
    }
  }

  @ParameterizedTest
  @JsonSource(
      """
      [
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          expectedErrorMessages: [
            'catalogName is required'
          ]
        },
        {
          databaseClass: 'liquibase.database.core.MSSQLDatabase',
          catalogName: 'cat1',
          expectedErrorMessages: []
        }
      ]
      """)
  void validate(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      List<String> expectedErrorMessages)
      throws Exception {
    // given
    var precondition = new ConnectionCatalogPrecondition();
    precondition.setCatalogName(catalogName);
    try (var database = DatabaseBuilder.of(databaseClass).build()) {

      // when
      var errors = precondition.validate(database);

      // then
      assertThat(errors.getErrorMessages())
          .containsExactlyInAnyOrderElementsOf(expectedErrorMessages);
      assertThat(errors.hasErrors()).isEqualTo(!expectedErrorMessages.isEmpty());
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock = """
          # catalogName
          AdventureWorks2022
          """,
      delimiter = '|')
  void check(@Nullable String catalogName) throws Exception {
    // given
    var precondition = new ConnectionCatalogPrecondition();
    precondition.setCatalogName(catalogName);
    try (var database =
        DatabaseBuilder.of(MarkitectMssqlDatabase.class)
            .withOfflineConnection(
                ocb -> ocb.withSnapshot("snapshots/mssql/AdventureWorks2022.json"))
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo("AdventureWorks2022");

      // when
      var thrown = catchThrowable(() -> precondition.check(database, null, null, null));

      // then
      assertThat(thrown).isNull();
    }
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
          # catalogName
          cat1
          """, delimiter = '|')
  void checkThrowsPreconditionErrorException(@Nullable String catalogName)
      throws DatabaseException {
    // given
    var precondition = new ConnectionCatalogPrecondition();
    precondition.setCatalogName(catalogName);
    try (var database = DatabaseBuilder.of(MarkitectMssqlDatabase.class).build()) {

      // when
      var thrown = catchThrowable(() -> precondition.check(database, null, null, null));

      // then
      assertThat(thrown).isInstanceOf(PreconditionErrorException.class);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # catalogName | expectedMessage
          master        | Connection catalog precondition failed: expected master, was AdventureWorks2022
          cat1          | Connection catalog precondition failed: expected cat1, was AdventureWorks2022
          """,
      delimiter = '|')
  void checkThrowsPreconditionFailedException(@Nullable String catalogName, String expectedMessage)
      throws DatabaseException {
    // given
    var precondition = new ConnectionCatalogPrecondition();
    precondition.setCatalogName(catalogName);
    try (var database =
        DatabaseBuilder.of(MarkitectMssqlDatabase.class)
            .withOfflineConnection(
                ocb -> ocb.withSnapshot("snapshots/mssql/AdventureWorks2022.json"))
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo("AdventureWorks2022");

      // when
      var thrown = catchThrowable(() -> precondition.check(database, null, null, null));

      // then
      assertThat(thrown)
          .asInstanceOf(type(PreconditionFailedException.class))
          .extracting(PreconditionFailedException::getFailedPreconditions)
          .asInstanceOf(list(FailedPrecondition.class))
          .extracting(FailedPrecondition::getMessage)
          .containsExactly(expectedMessage);
    }
  }
}
