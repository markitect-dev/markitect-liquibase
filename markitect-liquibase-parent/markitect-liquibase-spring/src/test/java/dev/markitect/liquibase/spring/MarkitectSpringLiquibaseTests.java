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

package dev.markitect.liquibase.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.Closeable;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkitectSpringLiquibaseTests {
  @Mock private CloseableDataSource dataSource;
  @InjectMocks private final MarkitectSpringLiquibase liquibase = new MarkitectSpringLiquibase();
  private final Map<String, Object> scopeValues =
      new LinkedHashMap<>(Map.of(LiquibaseCommandLineConfiguration.SHOULD_RUN.getKey(), false));

  @Test
  void shouldCloseDataSourceOnceMigratedOnly() throws Exception {
    // given
    liquibase.setCloseDataSourceOnceMigrated(true);

    // when
    Scope.child(scopeValues, liquibase::afterPropertiesSet);

    // then
    verify(dataSource).close();

    // when
    liquibase.destroy();

    // then
    verifyNoMoreInteractions(dataSource);
  }

  @Test
  void shouldCloseDataSourceOnDestroyOnly() throws Exception {
    // when
    Scope.child(scopeValues, liquibase::afterPropertiesSet);

    // then
    verifyNoInteractions(dataSource);

    // when
    liquibase.destroy();

    // then
    verify(dataSource).close();
    verifyNoMoreInteractions(dataSource);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          outputDefaultCatalog | outputDefaultSchema
          false                | false
          false                | true
          true                 | false
          true                 | true
          """,
      useHeadersInDisplayName = true,
      delimiter = '|')
  @SuppressWarnings("ConstantValue")
  void shouldCreateDatabase(boolean outputDefaultCatalog, boolean outputDefaultSchema)
      throws DatabaseException {
    // given
    liquibase.setOutputDefaultCatalog(outputDefaultCatalog);
    liquibase.setOutputDefaultSchema(outputDefaultSchema);
    Connection connection = null;
    var resourceAccessor = new ClassLoaderResourceAccessor();

    // when
    var database = liquibase.createDatabase(connection, resourceAccessor);

    // then
    assertThat(database.getOutputDefaultCatalog()).isEqualTo(outputDefaultCatalog);
    assertThat(database.getOutputDefaultSchema()).isEqualTo(outputDefaultSchema);
  }

  @SuppressWarnings("unused")
  interface CloseableDataSource extends DataSource, Closeable {}
}
