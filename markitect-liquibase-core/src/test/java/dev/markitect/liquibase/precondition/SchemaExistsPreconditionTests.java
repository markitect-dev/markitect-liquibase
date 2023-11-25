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

import dev.markitect.liquibase.database.DatabaseBuilder;
import java.util.List;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.json.JsonSource;

class SchemaExistsPreconditionTests {
  @Test
  void test() {
    // when
    var precondition = new SchemaExistsPrecondition();
    precondition.setCatalogName("Cat1");
    precondition.setSchemaName("Sch1");

    // then
    assertThat(precondition.getCatalogName()).isEqualTo("Cat1");
    assertThat(precondition.getSchemaName()).isEqualTo("Sch1");
    assertThat(precondition.getSerializedObjectNamespace())
        .isEqualTo(GENERIC_CHANGELOG_EXTENSION_NAMESPACE);
    assertThat(precondition.getSerializedObjectName()).isEqualTo("schemaExists");
  }

  @ParameterizedTest
  @JsonSource(
      """
      [
        {
          databaseClass: 'dev.markitect.liquibase.database.h2.MarkitectH2Database',
          expectedMessages: []
        },
        {
          databaseClass: 'dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase',
          expectedMessages: []
        },
        {
          databaseClass: 'dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase',
          expectedMessages: []
        },
        {
          databaseClass: 'dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase',
          expectedMessages: []
        }
      ]
      """)
  void warn(Class<? extends Database> databaseClass, List<String> expectedMessages) {
    // given
    var precondition = new SchemaExistsPrecondition();
    var database = DatabaseBuilder.of(databaseClass).build();

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
          databaseClass: 'dev.markitect.liquibase.database.h2.MarkitectH2Database',
          expectedErrorMessages: [
            'schemaName is required'
          ]
        },
        { databaseClass: 'dev.markitect.liquibase.database.h2.MarkitectH2Database',
          schemaName: 'Sch1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase',
          schemaName: 'Sch1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase',
          schemaName: 'Sch1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase',
          catalogName: 'Cat1',
          schemaName: 'Sch1',
          expectedErrorMessages: []
        },
        {
          databaseClass: 'dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase',
          schemaName: 'Sch1',
          expectedErrorMessages: []
        }
      ]
      """)
  void validate(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      String schemaName,
      List<String> expectedErrorMessages) {
    // given
    var precondition = new SchemaExistsPrecondition();
    precondition.setCatalogName(catalogName);
    precondition.setSchemaName(schemaName);
    var database = DatabaseBuilder.of(databaseClass).build();

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
          # databaseClass                                                       | connectionCatalogName | connectionSchemaName | catalogName | schemaName
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | PUBLIC                | PUBLIC               |             | PUBLIC
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | PUBLIC                | PUBLIC               | PUBLIC      | PUBLIC
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | PUBLIC                | PUBLIC               |             | PUBLIC
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | PUBLIC                | PUBLIC               | PUBLIC      | PUBLIC
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  |             | dbo
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  | Cat1        | dbo
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | public               |             | public
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | public               | Cat1        | public
          """,
      delimiter = '|')
  void check(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      @Nullable String catalogName,
      @Nullable String schemaName)
      throws Exception {
    // given
    var precondition = new SchemaExistsPrecondition();
    precondition.setCatalogName(catalogName);
    precondition.setSchemaName(schemaName);
    try (var database =
        DatabaseBuilder.of(databaseClass)
            .withOfflineConnection(
                ocb -> ocb.withCatalog(connectionCatalogName).withSchema(connectionSchemaName))
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
          # databaseClass                                                       | connectionCatalogName | connectionSchemaName | catalogName | schemaName
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | PUBLIC                | PUBLIC               |             | Sch1
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | PUBLIC                | PUBLIC               | Cat1        | PUBLIC
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | PUBLIC                | PUBLIC               |             | Sch1
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | PUBLIC                | PUBLIC               | Cat1        | PUBLIC
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  |             | Sch1
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | public               |             | Sch1
          """,
      delimiter = '|')
  void checkThrowsPreconditionFailedException(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      @Nullable String catalogName,
      @Nullable String schemaName)
      throws Exception {
    // given
    var precondition = new SchemaExistsPrecondition();
    precondition.setCatalogName(catalogName);
    precondition.setSchemaName(schemaName);
    try (var database =
        DatabaseBuilder.of(databaseClass)
            .withOfflineConnection(
                ocb -> ocb.withCatalog(connectionCatalogName).withSchema(connectionSchemaName))
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo(connectionCatalogName);
      assertThat(database.getDefaultSchemaName()).isEqualTo(connectionSchemaName);
      assertThat(precondition.warn(database).hasWarnings()).isFalse();
      assertThat(precondition.validate(database).hasErrors()).isFalse();

      // when
      var thrown = catchThrowable(() -> precondition.check(database, null, null, null));

      // then
      assertThat(thrown).isInstanceOf(PreconditionFailedException.class);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                                                       | connectionCatalogName | connectionSchemaName | catalogName | schemaName
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  | Cat2        | dbo
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | public               | Cat2        | public
          """,
      delimiter = '|')
  void checkThrowsPreconditionErrorException(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      @Nullable String catalogName,
      @Nullable String schemaName)
      throws Exception {
    // given
    var precondition = new SchemaExistsPrecondition();
    precondition.setCatalogName(catalogName);
    precondition.setSchemaName(schemaName);
    try (var database =
        DatabaseBuilder.of(databaseClass)
            .withOfflineConnection(
                ocb -> ocb.withCatalog(connectionCatalogName).withSchema(connectionSchemaName))
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
