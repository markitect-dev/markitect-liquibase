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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.database.TestDatabaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ResolvableType;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = TestDatabaseConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class SchemaExistsPreconditionIntegrationTests {
  @Autowired private BeanFactory beanFactory;

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                                                       | catalogName | schemaName
          dev.markitect.liquibase.database.h2.MarkitectH2Database               |             | PUBLIC
          dev.markitect.liquibase.database.h2.MarkitectH2Database               |             | lbschem2
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | lbcat       | PUBLIC
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | lbcat       | lbschem2
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         |             | PUBLIC
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         |             | lbschem2
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | PUBLIC      | PUBLIC
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | PUBLIC      | lbschem2
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         |             | dbo
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         |             | lbschem2
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat       | dbo
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat       | lbschem2
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat2      | dbo
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat2      | lbschem2
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | master      | dbo
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase |             | public
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase |             | lbschem2
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat       | public
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat       | lbschem2
          """,
      delimiter = '|')
  void check(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      @Nullable String schemaName)
      throws Exception {
    // given
    var precondition = new SchemaExistsPrecondition();
    precondition.setCatalogName(catalogName);
    precondition.setSchemaName(schemaName);
    try (var database = toDatabaseBuilder(databaseClass).build()) {
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
          # databaseClass                                                       | catalogName | schemaName
          dev.markitect.liquibase.database.h2.MarkitectH2Database               |             | lbschem3
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | lbcat       | lbschem3
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | lbcat2      | PUBLIC
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         |             | lbschem3
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | PUBLIC      | lbschem3
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | lbcat       | PUBLIC
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         |             | lbschem3
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat       | lbschem3
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat2      | lbschem3
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat3      | dbo
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | master      | lbschem2
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase |             | lbschem3
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat       | lbschem3
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat3      | public
          """,
      delimiter = '|')
  void check_throwsPreconditionFailedException(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      @Nullable String schemaName)
      throws Exception {
    // given
    var precondition = new SchemaExistsPrecondition();
    precondition.setCatalogName(catalogName);
    precondition.setSchemaName(schemaName);
    try (var database = toDatabaseBuilder(databaseClass).build()) {
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
          # databaseClass                                                       | catalogName | schemaName
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat2      | public
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat2      | lbschem2
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat2      | lbschem3
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | postgres    | public
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | postgres    | lbschem2
          """,
      delimiter = '|')
  void check_throwsPreconditionErrorException(
      Class<? extends Database> databaseClass,
      @Nullable String catalogName,
      @Nullable String schemaName)
      throws Exception {
    // given
    var precondition = new SchemaExistsPrecondition();
    precondition.setCatalogName(catalogName);
    precondition.setSchemaName(schemaName);
    try (var database = toDatabaseBuilder(databaseClass).build()) {
      assertThat(precondition.warn(database).hasWarnings()).isFalse();
      assertThat(precondition.validate(database).hasErrors()).isFalse();

      // when
      var thrown = catchThrowable(() -> precondition.check(database, null, null, null));

      // then
      assertThat(thrown).isInstanceOf(PreconditionErrorException.class);
    }
  }

  private <D extends Database> DatabaseBuilder<D> toDatabaseBuilder(Class<D> databaseClass) {
    return beanFactory
        .<DatabaseBuilder<D>>getBeanProvider(
            ResolvableType.forClassWithGenerics(DatabaseBuilder.class, databaseClass))
        .getObject();
  }
}
