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
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.sql.Connection;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class MarkitectSpringLiquibaseTests {
  @Mock
  @SuppressWarnings("unused")
  private DataSource dataSource;

  @Mock
  @SuppressWarnings("unused")
  private ResourceLoader resourceLoader;

  @InjectMocks private final MarkitectSpringLiquibase liquibase = new MarkitectSpringLiquibase();

  @BeforeEach
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void setUp() {
    liquibase.setBeanName("liquibase");
    assertThat(liquibase.getBeanName()).isNotNull();
    assertThatNoException().isThrownBy(liquibase::toString);
  }

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # outputDefaultCatalog | outputDefaultSchema
          false                  | false
          false                  | true
          true                   | false
          true                   | true
          """,
      delimiter = '|')
  @SuppressWarnings("ConstantValue")
  void shouldCreateDatabase(boolean outputDefaultCatalog, boolean outputDefaultSchema)
      throws Exception {
    // given
    liquibase.setOutputDefaultCatalog(outputDefaultCatalog);
    liquibase.setOutputDefaultSchema(outputDefaultSchema);
    Connection connection = null;
    var resourceAccessor = new SpringResourceAccessor(resourceLoader);

    // when
    var database = liquibase.createDatabase(connection, resourceAccessor);

    // then
    assertThat(database.getOutputDefaultCatalog()).isEqualTo(outputDefaultCatalog);
    assertThat(database.getOutputDefaultSchema()).isEqualTo(outputDefaultSchema);
  }
}
