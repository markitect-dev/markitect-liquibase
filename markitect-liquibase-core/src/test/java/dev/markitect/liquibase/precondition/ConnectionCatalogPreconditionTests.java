/*
 * Copyright 2023-2025 Markitect
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
import java.util.LinkedHashMap;
import java.util.List;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.FailedPrecondition;
import org.jspecify.annotations.Nullable;
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
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {

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
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {

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
      textBlock =
          """
          # databaseClass                                               | connectionCatalogName | connectionSchemaName | preserveSchemaCase | quotingStrategy   | catalogName
          dev.markitect.liquibase.database.h2.MarkitectH2Database       | CAT1                  | PUBLIC               |                    |                   | Cat1
          dev.markitect.liquibase.database.h2.MarkitectH2Database       | CAT1                  | PUBLIC               |                    | QUOTE_ALL_OBJECTS | Cat1
          dev.markitect.liquibase.database.h2.MarkitectH2Database       | CAT1                  | PUBLIC               | true               |                   | Cat1
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase | Cat1                  | dbo                  |                    |                   | Cat1
          """,
      delimiter = '|')
  void check(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      @Nullable Boolean preserveSchemaCase,
      @Nullable ObjectQuotingStrategy quotingStrategy,
      @Nullable String catalogName)
      throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    if (preserveSchemaCase != null) {
      scopeValues.put(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey(), preserveSchemaCase);
    }
    var precondition = new ConnectionCatalogPrecondition();
    precondition.setCatalogName(catalogName);
    try (var database =
        DatabaseBuilder.newBuilder(databaseClass)
            .objectQuotingStrategy(quotingStrategy)
            .offlineConnection(
                ocb -> ocb.catalog(connectionCatalogName).schema(connectionSchemaName))
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo(connectionCatalogName);
      assertThat(database.getDefaultSchemaName()).isEqualTo(connectionSchemaName);
      assertThat(precondition.warn(database).hasWarnings()).isFalse();
      assertThat(precondition.validate(database).hasErrors()).isFalse();

      // when
      var thrown =
          catchThrowable(
              () -> Scope.child(scopeValues, () -> precondition.check(database, null, null, null)));

      // then
      assertThat(thrown).isNull();
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                                               | catalogName
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase | Cat2
          """,
      delimiter = '|')
  void check_throwsPreconditionErrorException(
      Class<? extends Database> databaseClass, @Nullable String catalogName) throws Exception {
    // given
    var precondition = new ConnectionCatalogPrecondition();
    precondition.setCatalogName(catalogName);
    try (var database = DatabaseBuilder.newBuilder(databaseClass).build()) {
      assertThat(precondition.warn(database).hasWarnings()).isFalse();
      assertThat(precondition.validate(database).hasErrors()).isFalse();

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
          # databaseClass                                                | connectionCatalogName | connectionSchemaName | catalogName | expectedMessage
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase  | Cat1                  | dbo                  | master      | Connection catalog precondition failed: expected master, was Cat1
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase  | Cat1                  | dbo                  | Cat2        | Connection catalog precondition failed: expected Cat2, was Cat1
          """,
      delimiter = '|')
  void check_throwsPreconditionFailedException(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      @Nullable String catalogName,
      String expectedMessage)
      throws Exception {
    // given
    var precondition = new ConnectionCatalogPrecondition();
    precondition.setCatalogName(catalogName);
    try (var database =
        DatabaseBuilder.newBuilder(databaseClass)
            .offlineConnection(
                ocb -> ocb.catalog(connectionCatalogName).schema(connectionSchemaName))
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo(connectionCatalogName);
      assertThat(database.getDefaultSchemaName()).isEqualTo(connectionSchemaName);
      assertThat(precondition.warn(database).hasWarnings()).isFalse();
      assertThat(precondition.validate(database).hasErrors()).isFalse();

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
