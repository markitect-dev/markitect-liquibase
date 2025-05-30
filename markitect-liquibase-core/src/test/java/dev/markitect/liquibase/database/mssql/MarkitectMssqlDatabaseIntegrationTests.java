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

package dev.markitect.liquibase.database.mssql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.google.common.collect.Lists;
import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.database.DatabaseConnectionBuilder;
import dev.markitect.liquibase.database.TestDatabaseConfiguration;
import dev.markitect.liquibase.database.TestDatabaseSpecs;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.RollbackToDateCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.structure.DatabaseObject;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.json.JsonSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = TestDatabaseConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class MarkitectMssqlDatabaseIntegrationTests {
  @Autowired private DatabaseBuilder<MarkitectMssqlDatabase> databaseBuilder;
  @Autowired private TestDatabaseSpecs specs;
  @Autowired private MSSQLServerContainer<?> container;

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # preserveSchemaCase | quotingStrategy   | objectName | objectType                      | expected
                               |                   |            | liquibase.structure.core.Table  |
                               |                   | Tbl1       | liquibase.structure.core.Table  | Tbl1
                               | QUOTE_ALL_OBJECTS | Tbl1       | liquibase.structure.core.Table  | Tbl1
                               |                   | Sch1       | liquibase.structure.core.Schema | Sch1
          true                 |                   | Sch1       | liquibase.structure.core.Schema | Sch1
                               |                   | Tbl 1      | liquibase.structure.core.Table  | Tbl 1
                               | QUOTE_ALL_OBJECTS | Tbl 1      | liquibase.structure.core.Table  | Tbl 1
                               |                   | Sch 1      | liquibase.structure.core.Schema | Sch 1
          true                 |                   | Sch 1      | liquibase.structure.core.Schema | Sch 1
          """,
      delimiter = '|')
  void correctObjectName(
      @Nullable Boolean preserveSchemaCase,
      @Nullable ObjectQuotingStrategy quotingStrategy,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType,
      @Nullable String expected)
      throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    if (preserveSchemaCase != null) {
      scopeValues.put(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey(), preserveSchemaCase);
    }
    try (var database = databaseBuilder.objectQuotingStrategy(quotingStrategy).build()) {

      // when
      String actual =
          Scope.child(scopeValues, () -> database.correctObjectName(objectName, objectType));

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # includeCatalog | outputDefaultCatalog | outputDefaultSchema | catalogName | schemaName | objectName | objectType                     | expected
                           |                      |                     |             |            | Idx1       | liquibase.structure.core.Index | Idx1
                           |                      |                     |             | dbo        | Idx1       | liquibase.structure.core.Index | Idx1
          """,
      delimiter = '|')
  void escapeObjectName_catalogName_schemaName_objectName_objectType(
      @Nullable Boolean includeCatalog,
      @Nullable Boolean outputDefaultCatalog,
      @Nullable Boolean outputDefaultSchema,
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType,
      @Nullable String expected)
      throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    if (includeCatalog != null) {
      scopeValues.put(
          GlobalConfiguration.INCLUDE_CATALOG_IN_SPECIFICATION.getKey(), includeCatalog);
    }
    try (var database =
        databaseBuilder
            .outputDefaultCatalog(outputDefaultCatalog)
            .outputDefaultSchema(outputDefaultSchema)
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo("lbcat");
      assertThat(database.getDefaultSchemaName()).isEqualTo("dbo");

      // when
      String actual =
          Scope.child(
              scopeValues,
              () -> database.escapeObjectName(catalogName, schemaName, objectName, objectType));

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # preserveSchemaCase | quotingStrategy   | objectName | objectType                      | expected
                               |                   |            | liquibase.structure.core.Table  |
                               |                   | Tbl1       | liquibase.structure.core.Table  | Tbl1
                               | QUOTE_ALL_OBJECTS | Tbl1       | liquibase.structure.core.Table  | [Tbl1]
                               |                   | Sch1       | liquibase.structure.core.Schema | Sch1
          true                 |                   | Sch1       | liquibase.structure.core.Schema | Sch1
                               |                   | Tbl 1      | liquibase.structure.core.Table  | [Tbl 1]
                               | QUOTE_ALL_OBJECTS | Tbl 1      | liquibase.structure.core.Table  | [Tbl 1]
                               |                   | Sch 1      | liquibase.structure.core.Schema | [Sch 1]
          true                 |                   | Sch 1      | liquibase.structure.core.Schema | [Sch 1]
          """,
      delimiter = '|')
  void escapeObjectName(
      @Nullable Boolean preserveSchemaCase,
      @Nullable ObjectQuotingStrategy quotingStrategy,
      @Nullable String objectName,
      Class<? extends DatabaseObject> objectType,
      @Nullable String expected)
      throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    if (preserveSchemaCase != null) {
      scopeValues.put(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey(), preserveSchemaCase);
    }
    try (var database = databaseBuilder.objectQuotingStrategy(quotingStrategy).build()) {

      // when
      String actual =
          Scope.child(scopeValues, () -> database.escapeObjectName(objectName, objectType));

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # includeCatalog | outputDefaultCatalog | outputDefaultSchema | catalogName | schemaName | tableName | expected
                           |                      |                     |             |            | Tbl1      | dbo.Tbl1
                           |                      |                     |             | dbo        | Tbl1      | dbo.Tbl1
                           |                      | false               |             |            | Tbl1      | Tbl1
                           |                      | false               |             | dbo        | Tbl1      | Tbl1
                           |                      | false               |             | lbschem2   | Tbl1      | lbschem2.Tbl1
          true             |                      |                     |             |            | Tbl1      | lbcat.dbo.Tbl1
          true             |                      |                     |             | dbo        | Tbl1      | lbcat.dbo.Tbl1
          true             |                      | false               |             |            | Tbl1      | lbcat..Tbl1
          true             |                      | false               |             | dbo        | Tbl1      | lbcat..Tbl1
          true             | false                | false               |             |            | Tbl1      | Tbl1
          true             | false                | false               |             | dbo        | Tbl1      | Tbl1
                           | false                |                     | lbcat2      |            | Tbl1      | lbcat2..Tbl1
                           | false                |                     | lbcat2      | dbo        | Tbl1      | lbcat2.dbo.Tbl1
          """,
      delimiter = '|')
  void escapeTableName(
      @Nullable Boolean includeCatalog,
      @Nullable Boolean outputDefaultCatalog,
      @Nullable Boolean outputDefaultSchema,
      @Nullable String catalogName,
      @Nullable String schemaName,
      @Nullable String tableName,
      @Nullable String expected)
      throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    if (includeCatalog != null) {
      scopeValues.put(
          GlobalConfiguration.INCLUDE_CATALOG_IN_SPECIFICATION.getKey(), includeCatalog);
    }
    try (var database =
        databaseBuilder
            .outputDefaultCatalog(outputDefaultCatalog)
            .outputDefaultSchema(outputDefaultSchema)
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo("lbcat");
      assertThat(database.getDefaultSchemaName()).isEqualTo("dbo");

      // when
      String actual =
          Scope.child(
              scopeValues, () -> database.escapeTableName(catalogName, schemaName, tableName));

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @ParameterizedTest
  @JsonSource(
      """
      [
        [
          {
            databaseName: 'master',
            changeLogFileName: 'db/changelog/mssql/database/master/db.changelog.xml'
          },
          {
            databaseName: 'AdventureWorks2022',
            changeLogFileName: 'db/changelog/mssql/database/AdventureWorks2022/db.changelog.xml'
          },
          {
            databaseName: 'Northwind',
            changeLogFileName: 'db/changelog/common/database/Northwind/db.changelog.xml'
          }
        ],
        [
          {
            databaseName: 'master',
            changeLogFileName: 'db/changelog/mssql/database/master/db.changelog.yaml'
          },
          {
            databaseName: 'AdventureWorks2022',
            changeLogFileName: 'db/changelog/mssql/database/AdventureWorks2022/db.changelog.yaml'
          },
          {
            databaseName: 'Northwind',
            changeLogFileName: 'db/changelog/common/database/Northwind/db.changelog.yaml'
          }
        ]
      ]
      """)
  void updateAndRollback(List<DatabaseRecord> databaseRecords) {
    // when
    var thrown =
        catchThrowable(
            () -> {
              for (var databaseRecord : databaseRecords) {
                try (var database =
                    databaseBuilder
                        .databaseConnection(
                            DatabaseConnectionBuilder.newBuilder()
                                .url(toJdbcUrl(databaseRecord))
                                .username(specs.getUsername())
                                .password(specs.getPassword())
                                .driver(container.getDriverClassName()))
                        .build()) {
                  var scopeValues = new LinkedHashMap<String, Object>();
                  scopeValues.put(Scope.Attr.database.name(), database);
                  Scope.child(
                      scopeValues,
                      () ->
                          new CommandScope(UpdateCommandStep.COMMAND_NAME)
                              .addArgumentValue(
                                  DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                              .addArgumentValue(
                                  UpdateCommandStep.CHANGELOG_FILE_ARG,
                                  databaseRecord.changeLogFileName())
                              .execute());
                }
              }
              for (var databaseRecord : Lists.reverse(databaseRecords)) {
                try (var database =
                    databaseBuilder
                        .databaseConnection(
                            DatabaseConnectionBuilder.newBuilder()
                                .url(toJdbcUrl(databaseRecord))
                                .username(specs.getUsername())
                                .password(specs.getPassword())
                                .driver(container.getDriverClassName()))
                        .build()) {
                  var scopeValues = new LinkedHashMap<String, Object>();
                  scopeValues.put(Scope.Attr.database.name(), database);
                  Scope.child(
                      scopeValues,
                      () ->
                          new CommandScope(RollbackToDateCommandStep.COMMAND_NAME)
                              .addArgumentValue(
                                  DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                              .addArgumentValue(
                                  DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG,
                                  databaseRecord.changeLogFileName())
                              .addArgumentValue(
                                  RollbackToDateCommandStep.DATE_ARG, Date.from(Instant.EPOCH))
                              .execute());
                }
              }
            });

    // then
    assertThat(thrown).isNull();
  }

  private String toJdbcUrl(DatabaseRecord databaseRecord) {
    return container.getJdbcUrl() + ";databaseName=" + databaseRecord.databaseName();
  }

  @SuppressWarnings("unused")
  private record DatabaseRecord(String databaseName, String changeLogFileName) {}
}
