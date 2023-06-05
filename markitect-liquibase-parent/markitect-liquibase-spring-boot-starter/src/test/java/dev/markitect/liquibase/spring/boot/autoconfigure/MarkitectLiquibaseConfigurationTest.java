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

package dev.markitect.liquibase.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.BDDMockito.given;

import dev.markitect.liquibase.spring.MarkitectSpringLiquibase;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.MarkitectLiquibaseConfiguration;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MarkitectLiquibaseConfigurationTest {
  private final LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
  private final MarkitectLiquibaseProperties markitectLiquibaseProperties =
      new MarkitectLiquibaseProperties();
  private final MarkitectLiquibaseConfiguration markitectLiquibaseConfiguration =
      new MarkitectLiquibaseConfiguration(liquibaseProperties, markitectLiquibaseProperties);
  @Mock private ObjectProvider<DataSource> liquibaseDataSourceProvider;
  @Mock private DataSource liquibaseDataSource;
  @Mock private ObjectProvider<DataSource> dataSourceProvider;
  @Mock private DataSource dataSource;

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void failsToCreateLiquibaseBeanWithoutDataSourceOrLiquibaseUrl() {
    // when/then
    assertThatThrownBy(
            () ->
                markitectLiquibaseConfiguration.liquibase(
                    liquibaseDataSourceProvider, dataSourceProvider))
        .isExactlyInstanceOf(IllegalStateException.class)
        .hasMessage("Liquibase migration DataSource missing");
  }

  @Test
  void createsLiquibaseBeanUsingLiquibaseDataSource() {
    // given
    given(liquibaseDataSourceProvider.getIfAvailable()).willReturn(liquibaseDataSource);

    // when
    var liquibase =
        markitectLiquibaseConfiguration.liquibase(liquibaseDataSourceProvider, dataSourceProvider);

    // then
    assertThat(liquibase)
        .asInstanceOf(type(MarkitectSpringLiquibase.class))
        .extracting(SpringLiquibase::getDataSource)
        .isSameAs(liquibaseDataSource);
    assertThat(ReflectionTestUtils.getField(liquibase, "closeDataSourceOnceMigrated"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isFalse();
  }

  @Test
  void createsLiquibaseBeanUsingDataSource() {
    // given
    given(dataSourceProvider.getIfUnique()).willReturn(dataSource);

    // when
    var liquibase =
        markitectLiquibaseConfiguration.liquibase(liquibaseDataSourceProvider, dataSourceProvider);

    // then
    assertThat(liquibase)
        .asInstanceOf(type(MarkitectSpringLiquibase.class))
        .extracting(SpringLiquibase::getDataSource)
        .isSameAs(dataSource);
    assertThat(ReflectionTestUtils.getField(liquibase, "closeDataSourceOnceMigrated"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isFalse();
  }

  @Test
  void createsLiquibaseBeanUsingLiquibaseUrl() {
    // given
    String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false";
    liquibaseProperties.setDriverClassName("org.h2.Driver");
    liquibaseProperties.setUrl(url);
    liquibaseProperties.setUser("dbuser");
    liquibaseProperties.setPassword("letmein");

    // when
    var liquibase =
        markitectLiquibaseConfiguration.liquibase(liquibaseDataSourceProvider, dataSourceProvider);

    // then
    assertThat(liquibase)
        .asInstanceOf(type(MarkitectSpringLiquibase.class))
        .extracting(SpringLiquibase::getDataSource)
        .asInstanceOf(type(SimpleDriverDataSource.class))
        .extracting(
            migrationDataSource ->
                Optional.ofNullable(migrationDataSource.getDriver())
                    .map(Object::getClass)
                    .orElse(null),
            SimpleDriverDataSource::getUrl,
            SimpleDriverDataSource::getUsername,
            SimpleDriverDataSource::getPassword)
        .containsExactly(org.h2.Driver.class, url, "dbuser", "letmein");
    assertThat(ReflectionTestUtils.getField(liquibase, "closeDataSourceOnceMigrated"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isTrue();
  }

  @Test
  void createsLiquibaseBeanUsingDataSourceUrl() {
    // given
    String url =
        "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false".formatted(UUID.randomUUID());
    var dataSource = new SimpleDriverDataSource();
    dataSource.setDriverClass(org.h2.Driver.class);
    dataSource.setUrl(url);
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    given(dataSourceProvider.getIfUnique()).willReturn(dataSource);
    liquibaseProperties.setUser("dbuser");
    liquibaseProperties.setPassword("letmein");

    // when
    var liquibase =
        markitectLiquibaseConfiguration.liquibase(liquibaseDataSourceProvider, dataSourceProvider);

    // then
    assertThat(liquibase)
        .asInstanceOf(type(MarkitectSpringLiquibase.class))
        .extracting(SpringLiquibase::getDataSource)
        .isNotSameAs(dataSource)
        .asInstanceOf(type(SimpleDriverDataSource.class))
        .extracting(
            migrationDataSource ->
                Optional.ofNullable(migrationDataSource.getDriver())
                    .map(Object::getClass)
                    .orElse(null),
            SimpleDriverDataSource::getUrl,
            SimpleDriverDataSource::getUsername,
            SimpleDriverDataSource::getPassword)
        .containsExactly(org.h2.Driver.class, url, "dbuser", "letmein");
    assertThat(ReflectionTestUtils.getField(liquibase, "closeDataSourceOnceMigrated"))
        .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
        .isTrue();
  }
}
