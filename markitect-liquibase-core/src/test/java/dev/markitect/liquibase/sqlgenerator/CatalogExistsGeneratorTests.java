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

package dev.markitect.liquibase.sqlgenerator;

import static org.assertj.core.api.Assertions.assertThat;

import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.statement.CatalogExistsStatement;
import liquibase.database.Database;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CatalogExistsGeneratorTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
# databaseClass                                                       | connectionCatalogName | connectionSchemaName | catalogName | expectedSql
dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | Cat1                  | dbo                  | Cat2        | SELECT CAST(CASE WHEN DB_ID(N'Cat2') IS NOT NULL THEN 1 ELSE 0 END AS bit)
dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | Cat1                  | PUBLIC               | Cat2        | SELECT EXISTS(SELECT 1 FROM pg_catalog.pg_database WHERE datname = 'cat2')
""",
      delimiter = '|')
  void generateSql(
      Class<? extends Database> databaseClass,
      @Nullable String connectionCatalogName,
      @Nullable String connectionSchemaName,
      String catalogName,
      String expectedSql)
      throws Exception {
    // given
    var statement = new CatalogExistsStatement();
    statement.setCatalogName(catalogName);
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
          liquibase.database.core.H2Database       | false
          liquibase.database.core.HsqlDatabase     | false
          liquibase.database.core.MSSQLDatabase    | true
          liquibase.database.core.OracleDatabase   | false
          liquibase.database.core.PostgresDatabase | true
          """,
      delimiter = '|')
  void supports(Class<? extends Database> databaseClass, boolean expected) throws Exception {
    // given
    var statement = new CatalogExistsStatement();
    var generator = new CatalogExistsGenerator();
    try (var database = DatabaseBuilder.of(databaseClass).build()) {

      // when
      boolean actual = generator.supports(statement, database);

      // then
      assertThat(actual).isEqualTo(expected);
    }
  }
}
