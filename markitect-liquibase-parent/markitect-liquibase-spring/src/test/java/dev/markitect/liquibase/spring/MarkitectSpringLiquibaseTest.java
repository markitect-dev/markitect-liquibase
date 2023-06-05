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
import java.util.LinkedHashMap;
import javax.sql.DataSource;
import liquibase.Scope;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MarkitectSpringLiquibaseTest {
  @Mock private CloseableDataSource migrationDataSource;

  @Test
  void test() {
    // when
    var liquibase = new MarkitectSpringLiquibase();

    // then
    assertThat(liquibase.getDataSource()).isNull();
    assertThat(ReflectionTestUtils.getField(liquibase, "closeDataSourceOnceMigrated"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isFalse();
    assertThat(ReflectionTestUtils.getField(liquibase, "outputDefaultCatalog"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isFalse();
    assertThat(ReflectionTestUtils.getField(liquibase, "outputDefaultSchema"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isFalse();

    // when
    liquibase.setDataSource(migrationDataSource);
    liquibase.setCloseDataSourceOnceMigrated(true);
    liquibase.setOutputDefaultCatalog(true);
    liquibase.setOutputDefaultSchema(true);

    // then
    assertThat(liquibase.getDataSource()).isSameAs(migrationDataSource);
    assertThat(ReflectionTestUtils.getField(liquibase, "closeDataSourceOnceMigrated"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isTrue();
    assertThat(ReflectionTestUtils.getField(liquibase, "outputDefaultCatalog"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isTrue();
    assertThat(ReflectionTestUtils.getField(liquibase, "outputDefaultSchema"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isTrue();
  }

  @Test
  void closesDataSourceOnDestroyOnly() throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    scopeValues.put(LiquibaseCommandLineConfiguration.SHOULD_RUN.getKey(), false);
    var liquibase = new MarkitectSpringLiquibase();
    liquibase.setDataSource(migrationDataSource);

    // when
    Scope.child(scopeValues, liquibase::afterPropertiesSet);

    // then
    verifyNoInteractions(migrationDataSource);

    // when
    liquibase.destroy();

    // then
    verify(migrationDataSource).close();
  }

  @Test
  void closesDataSourceOnceMigratedOnly() throws Exception {
    // given
    var scopeValues = new LinkedHashMap<String, Object>();
    scopeValues.put(LiquibaseCommandLineConfiguration.SHOULD_RUN.getKey(), false);
    var liquibase = new MarkitectSpringLiquibase();
    liquibase.setDataSource(migrationDataSource);
    liquibase.setCloseDataSourceOnceMigrated(true);

    // when
    Scope.child(scopeValues, liquibase::afterPropertiesSet);

    // then
    verify(migrationDataSource).close();

    // when
    liquibase.destroy();

    // then
    verifyNoMoreInteractions(migrationDataSource);
  }

  interface CloseableDataSource extends DataSource, Closeable {}
}
