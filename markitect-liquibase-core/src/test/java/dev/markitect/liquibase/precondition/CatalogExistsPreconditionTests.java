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

package dev.markitect.liquibase.precondition;

import static liquibase.serializer.LiquibaseSerializable.GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.markitect.liquibase.database.DatabaseBuilder;
import java.util.List;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.json.JsonSource;

class CatalogExistsPreconditionTests {
  @Test
  void test() {
    // when
    var precondition = new CatalogExistsPrecondition();
    precondition.setCatalogName("Cat1");

    // then
    assertThat(precondition.getCatalogName()).isEqualTo("Cat1");
    assertThat(precondition.getSerializedObjectNamespace())
        .isEqualTo(GENERIC_CHANGELOG_EXTENSION_NAMESPACE);
    assertThat(precondition.getSerializedObjectName()).isEqualTo("catalogExists");
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
  void warn(Class<? extends Database> databaseClass, List<String> expectedMessages) {
    // given
    var precondition = new CatalogExistsPrecondition();
    var database = DatabaseBuilder.newBuilder(databaseClass).build();

    // when
    var warnings = precondition.warn(database);

    // then
    assertThat(warnings.hasWarnings()).isEqualTo(!expectedMessages.isEmpty());
    assertThat(warnings.getMessages()).containsExactlyInAnyOrderElementsOf(expectedMessages);
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
          catalogName: 'Cat1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'liquibase.database.core.PostgresDatabase',
          catalogName: 'Cat1',
          expectedErrorMessages: []
        }
      ]
      """)
  void validate(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      List<String> expectedErrorMessages) {
    // given
    var precondition = new CatalogExistsPrecondition();
    precondition.setCatalogName(catalogName);
    var database = DatabaseBuilder.newBuilder(databaseClass).build();

    // when
    var errors = precondition.validate(database);

    // then
    assertThat(errors.getErrorMessages())
        .containsExactlyInAnyOrderElementsOf(expectedErrorMessages);
    assertThat(errors.hasErrors()).isEqualTo(!expectedErrorMessages.isEmpty());
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
# databaseClass                                                       | connectionCatalogName | connectionSchemaName | catalogName
dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  | Cat1
dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | public               | Cat1
""",
      delimiter = '|')
  void check(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      @Nullable String catalogName)
      throws Exception {
    // given
    var precondition = new CatalogExistsPrecondition();
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
      assertThat(thrown).isNull();
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
# databaseClass                                                       | connectionCatalogName | connectionSchemaName | catalogName
dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  | Cat2
dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  | master
dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | public               | Cat2
dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | public               | postgres
""",
      delimiter = '|')
  void check_throwsPreconditionErrorException(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      @Nullable String catalogName)
      throws Exception {
    // given
    var precondition = new CatalogExistsPrecondition();
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
      assertThat(thrown).isInstanceOf(PreconditionErrorException.class);
    }
  }
}
