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

package dev.markitect.liquibase.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import dev.markitect.liquibase.spring.MarkitectSpringLiquibase;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseAutoConfigurationRuntimeHints;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseDataSourceCondition;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.MarkitectLiquibaseConfiguration;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.PropertiesLiquibaseConnectionDetails;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.SpringLiquibaseCustomizer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aot.hint.ResourceHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseConnectionDetails;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class MarkitectLiquibaseAutoConfigurationTests {
  private final MockEnvironment environment = new MockEnvironment();

  @Test
  void shouldCreateSpringLiquibaseBeanPostProcessor() {
    // when
    var springLiquibaseBeanPostProcessor =
        MarkitectLiquibaseAutoConfiguration.springLiquibaseBeanPostProcessor(environment);

    // then
    assertThat(springLiquibaseBeanPostProcessor).isNotNull();
  }

  @Nested
  class MarkitectLiquibaseConfigurationTests {
    @Spy private final LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
    private final MarkitectLiquibaseProperties markitectLiquibaseProperties =
        new MarkitectLiquibaseProperties();
    private final MarkitectLiquibaseConfiguration markitectLiquibaseConfiguration =
        new MarkitectLiquibaseConfiguration();
    @Mock private ObjectProvider<DataSource> dataSourceProvider;
    @Mock private ObjectProvider<SpringLiquibaseCustomizer> customizers;
    @Mock private ObjectProvider<DataSource> liquibaseDataSourceProvider;
    @Mock private DataSource liquibaseDataSource;
    @Mock private LiquibaseConnectionDetails connectionDetails;
    @Mock private ResourceLoader resourceLoader;

    @Test
    void shouldFailToCreateLiquibaseBeanWithNoDataSourceUrlOrLiquibaseUrl() {
      // when
      var thrown =
          catchThrowable(
              () ->
                  markitectLiquibaseConfiguration.liquibase(
                      dataSourceProvider,
                      liquibaseDataSourceProvider,
                      liquibaseProperties,
                      customizers,
                      connectionDetails,
                      markitectLiquibaseProperties));

      // then
      assertThat(thrown)
          .isExactlyInstanceOf(IllegalStateException.class)
          .hasMessage("Liquibase migration DataSource missing");
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.4.x", "3.5.x"})
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void shouldCreateLiquibaseBeanUsingLiquibaseDataSource(String springBootVersion) {
      // given
      given(liquibaseDataSourceProvider.getIfAvailable()).willReturn(liquibaseDataSource);
      if (springBootVersion.equals("3.4.x")) {
        given(liquibaseProperties.getAnalyticsEnabled()).willThrow(NoSuchMethodError.class);
        given(liquibaseProperties.getLicenseKey()).willThrow(NoSuchMethodError.class);
      }

      // when
      var liquibase =
          markitectLiquibaseConfiguration.liquibase(
              dataSourceProvider,
              liquibaseDataSourceProvider,
              liquibaseProperties,
              customizers,
              connectionDetails,
              markitectLiquibaseProperties);
      liquibase.setBeanName("liquibase");
      liquibase.setResourceLoader(resourceLoader);

      // then
      assertThat(liquibase.getBeanName()).isNotNull();
      assertThatNoException().isThrownBy(liquibase::toString);
      assertThat(liquibase)
          .asInstanceOf(type(MarkitectSpringLiquibase.class))
          .extracting(SpringLiquibase::getDataSource)
          .isSameAs(liquibaseDataSource);
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.4.x", "3.5.x"})
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void shouldCreateLiquibaseBeanUsingDataSource(String springBootVersion) {
      // given
      var dataSource = mock(DataSource.class);
      given(dataSourceProvider.getIfUnique()).willReturn(dataSource);
      if (springBootVersion.equals("3.4.x")) {
        given(liquibaseProperties.getAnalyticsEnabled()).willThrow(NoSuchMethodError.class);
        given(liquibaseProperties.getLicenseKey()).willThrow(NoSuchMethodError.class);
      }

      // when
      var liquibase =
          markitectLiquibaseConfiguration.liquibase(
              dataSourceProvider,
              liquibaseDataSourceProvider,
              liquibaseProperties,
              customizers,
              connectionDetails,
              markitectLiquibaseProperties);
      liquibase.setBeanName("liquibase");
      liquibase.setResourceLoader(resourceLoader);

      // then
      assertThat(liquibase.getBeanName()).isNotNull();
      assertThatNoException().isThrownBy(liquibase::toString);
      assertThat(liquibase)
          .asInstanceOf(type(MarkitectSpringLiquibase.class))
          .extracting(SpringLiquibase::getDataSource)
          .isSameAs(dataSource);
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.4.x", "3.5.x"})
    @SuppressFBWarnings("HARD_CODE_PASSWORD")
    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnnecessarilyFullyQualified"})
    void shouldCreateLiquibaseBeanUsingLiquibaseUrl(String springBootVersion) {
      // given
      String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false";
      liquibaseProperties.setDriverClassName("org.h2.Driver");
      liquibaseProperties.setUrl(url);
      liquibaseProperties.setUser("dbuser");
      liquibaseProperties.setPassword("letmein");
      given(connectionDetails.getJdbcUrl()).willAnswer(invocation -> liquibaseProperties.getUrl());
      given(connectionDetails.getUsername())
          .willAnswer(invocation -> liquibaseProperties.getUser());
      given(connectionDetails.getPassword())
          .willAnswer(invocation -> liquibaseProperties.getPassword());
      given(connectionDetails.getDriverClassName())
          .willAnswer(invocation -> liquibaseProperties.getDriverClassName());
      if (springBootVersion.equals("3.4.x")) {
        given(liquibaseProperties.getAnalyticsEnabled()).willThrow(NoSuchMethodError.class);
        given(liquibaseProperties.getLicenseKey()).willThrow(NoSuchMethodError.class);
      }

      // when
      var liquibase =
          markitectLiquibaseConfiguration.liquibase(
              dataSourceProvider,
              liquibaseDataSourceProvider,
              liquibaseProperties,
              customizers,
              connectionDetails,
              markitectLiquibaseProperties);
      liquibase.setBeanName("liquibase");
      liquibase.setResourceLoader(resourceLoader);

      // then
      assertThat(liquibase.getBeanName()).isNotNull();
      assertThatNoException().isThrownBy(liquibase::toString);
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
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.4.x", "3.5.x"})
    @SuppressFBWarnings("HARD_CODE_PASSWORD")
    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnnecessarilyFullyQualified"})
    void shouldCreateLiquibaseBeanUsingDataSourceUrl(String springBootVersion) {
      // given
      String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false";
      var dataSource = new SimpleDriverDataSource();
      dataSource.setDriverClass(org.h2.Driver.class);
      dataSource.setUrl(url);
      dataSource.setUsername("sa");
      dataSource.setPassword("");
      given(dataSourceProvider.getIfUnique()).willReturn(dataSource);
      liquibaseProperties.setUser("dbuser");
      liquibaseProperties.setPassword("letmein");
      given(connectionDetails.getUsername())
          .willAnswer(invocation -> liquibaseProperties.getUser());
      given(connectionDetails.getPassword())
          .willAnswer(invocation -> liquibaseProperties.getPassword());
      given(connectionDetails.getDriverClassName()).willReturn("org.h2.Driver");
      if (springBootVersion.equals("3.4.x")) {
        given(liquibaseProperties.getAnalyticsEnabled()).willThrow(NoSuchMethodError.class);
        given(liquibaseProperties.getLicenseKey()).willThrow(NoSuchMethodError.class);
      }

      // when
      var liquibase =
          markitectLiquibaseConfiguration.liquibase(
              dataSourceProvider,
              liquibaseDataSourceProvider,
              liquibaseProperties,
              customizers,
              connectionDetails,
              markitectLiquibaseProperties);
      liquibase.setBeanName("liquibase");
      liquibase.setResourceLoader(resourceLoader);

      // then
      assertThat(liquibase.getBeanName()).isNotNull();
      assertThatNoException().isThrownBy(liquibase::toString);
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
    }
  }

  @Nested
  class LiquibaseDataSourceConditionTests {
    private final LiquibaseDataSourceCondition liquibaseDataSourceCondition =
        new LiquibaseDataSourceCondition();

    @Test
    void configurationPhaseShouldBeRegisterBean() {
      // when
      var configurationPhase = liquibaseDataSourceCondition.getConfigurationPhase();

      // then
      assertThat(configurationPhase).isEqualTo(ConfigurationPhase.REGISTER_BEAN);
    }
  }

  @Nested
  class LiquibaseAutoConfigurationRuntimeHintsTests {
    private final LiquibaseAutoConfigurationRuntimeHints liquibaseAutoConfigurationRuntimeHints =
        new LiquibaseAutoConfigurationRuntimeHints();
    @Mock private RuntimeHints runtimeHints;
    @Mock private ResourceHints resourceHints;

    @Test
    void shouldRegisterHints() {
      // given
      given(runtimeHints.resources()).willReturn(resourceHints);

      // when
      liquibaseAutoConfigurationRuntimeHints.registerHints(runtimeHints, null);

      // then
      then(resourceHints).should().registerPattern("db/changelog/*");
    }
  }

  @Nested
  class PropertiesLiquibaseConnectionDetailsTests {
    private final LiquibaseProperties liquibaseProperties = new LiquibaseProperties();

    @Test
    @SuppressFBWarnings("HARD_CODE_PASSWORD")
    void shouldResolveProperties() {
      // given
      String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false";
      liquibaseProperties.setUrl(url);
      liquibaseProperties.setUser("dbuser");
      liquibaseProperties.setPassword("letmein");

      // when
      var connectionDetails = new PropertiesLiquibaseConnectionDetails(liquibaseProperties);

      // then
      assertThat(connectionDetails)
          .extracting(
              PropertiesLiquibaseConnectionDetails::getUsername,
              PropertiesLiquibaseConnectionDetails::getPassword,
              PropertiesLiquibaseConnectionDetails::getJdbcUrl,
              PropertiesLiquibaseConnectionDetails::getDriverClassName)
          .containsExactly("dbuser", "letmein", url, "org.h2.Driver");
    }
  }
}
