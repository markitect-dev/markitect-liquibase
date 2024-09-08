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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.database.TestDatabaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.PreconditionFailedException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ResolvableType;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = TestDatabaseConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class CatalogExistsPreconditionIntegrationTests {
  @Autowired private BeanFactory beanFactory;

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # databaseClass                                                       | catalogName
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat2
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | master
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat2
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | postgres
          """,
      delimiter = '|')
  void check(Class<? extends Database> databaseClass, @Nullable String catalogName)
      throws Exception {
    // given
    var precondition = new CatalogExistsPrecondition();
    precondition.setCatalogName(catalogName);
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
          # databaseClass                                                       | catalogName
          dev.markitect.liquibase.database.h2.MarkitectH2Database               | lbcat2
          dev.markitect.liquibase.database.hsqldb.MarkitectHsqlDatabase         | lbcat
          dev.markitect.liquibase.database.mssql.MarkitectMssqlDatabase         | lbcat3
          dev.markitect.liquibase.database.postgresql.MarkitectPostgresDatabase | lbcat3
          """,
      delimiter = '|')
  void check_throwsPreconditionFailedException(
      Class<? extends Database> databaseClass, @Nullable String catalogName) throws Exception {
    // given
    var precondition = new CatalogExistsPrecondition();
    precondition.setCatalogName(catalogName);
    try (var database = toDatabaseBuilder(databaseClass).build()) {
      assertThat(precondition.warn(database).hasWarnings()).isFalse();
      assertThat(precondition.validate(database).hasErrors()).isFalse();

      // when
      var thrown = catchThrowable(() -> precondition.check(database, null, null, null));

      // then
      assertThat(thrown).isInstanceOf(PreconditionFailedException.class);
    }
  }

  private <D extends Database> DatabaseBuilder<D> toDatabaseBuilder(Class<D> databaseClass) {
    return beanFactory
        .<DatabaseBuilder<D>>getBeanProvider(
            ResolvableType.forClassWithGenerics(DatabaseBuilder.class, databaseClass))
        .getObject();
  }
}
