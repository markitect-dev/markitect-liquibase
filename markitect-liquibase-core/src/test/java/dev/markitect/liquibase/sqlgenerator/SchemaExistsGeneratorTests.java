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

package dev.markitect.liquibase.sqlgenerator;

import static org.assertj.core.api.Assertions.assertThat;

import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.statement.SchemaExistsStatement;
import liquibase.database.Database;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SchemaExistsGeneratorTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                                                       | connectionCatalogName | connectionSchemaName | catalogName | schemaName | expectedSql
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | DEFAULT               | PUBLIC               |             | Sch1       | SELECT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'SCH1')
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | PUBLIC                | PUBLIC               |             | Sch1       | SELECT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'SCH1') FROM INFORMATION_SCHEMA.SYSTEM_USERS
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  |             | Sch1       | SELECT CAST(CASE WHEN EXISTS (SELECT 1 FROM sys.schemas WHERE name = N'Sch1') THEN 1 ELSE 0 END AS bit)
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  | Cat2        | Sch1       | SELECT CAST(CASE WHEN EXISTS (SELECT 1 FROM Cat2.sys.schemas WHERE name = N'Sch1') THEN 1 ELSE 0 END AS bit)
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | PUBLIC               |             | Sch1       | SELECT EXISTS(SELECT 1 FROM pg_catalog.pg_namespace WHERE nspname = 'sch1')
          """,
      delimiter = '|')
  void generateSql(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      @Nullable String catalogName,
      String schemaName,
      String expectedSql)
      throws Exception {
    // given
    var statement = new SchemaExistsStatement();
    statement.setCatalogName(catalogName);
    statement.setSchemaName(schemaName);
    try (var database =
        DatabaseBuilder.of(databaseClass)
            .withOfflineConnection(
                ocb -> ocb.withCatalog(connectionCatalogName).withSchema(connectionSchemaName))
            .build()) {
      assertThat(database.getDefaultCatalogName()).isEqualTo(connectionCatalogName);
      assertThat(database.getDefaultSchemaName()).isEqualTo(connectionSchemaName);

      // when
      var errors = SqlGeneratorFactory.getInstance().validate(statement, database);
      var sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);

      // then
      assertThat(errors.hasErrors()).isFalse();
      assertThat(sql)
          .usingRecursiveFieldByFieldElementComparator()
          .containsExactly(new UnparsedSql(expectedSql));
    }
  }

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
    var statement = new SchemaExistsStatement();
    var generator = new SchemaExistsGenerator();
    try (var database = DatabaseBuilder.of(databaseClass).build()) {

      // when
      boolean actual = generator.supports(statement, database);

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }
}
