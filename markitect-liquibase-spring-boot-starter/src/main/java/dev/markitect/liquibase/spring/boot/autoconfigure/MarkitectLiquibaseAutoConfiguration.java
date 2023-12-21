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

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;
import static dev.markitect.liquibase.base.Verify.verifyNotNull;

import dev.markitect.liquibase.spring.MarkitectSpringLiquibase;
import dev.markitect.liquibase.spring.SpringLiquibaseBeanPostProcessor;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseAutoConfigurationRuntimeHints;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseDataSourceCondition;
import java.util.Optional;
import javax.sql.DataSource;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.change.DatabaseChange;
import liquibase.integration.spring.SpringLiquibase;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseConnectionDetails;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@AutoConfiguration(
    before = LiquibaseAutoConfiguration.class,
    after = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ConditionalOnClass({SpringLiquibase.class, DatabaseChange.class})
@ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", matchIfMissing = true)
@Conditional(LiquibaseDataSourceCondition.class)
@Import(DatabaseInitializationDependencyConfigurer.class)
@ImportRuntimeHints(LiquibaseAutoConfigurationRuntimeHints.class)
public class MarkitectLiquibaseAutoConfiguration {
  @Bean
  public static SpringLiquibaseBeanPostProcessor springLiquibaseBeanPostProcessor(
      Environment environment) {
    return new SpringLiquibaseBeanPostProcessor(environment);
  }

  private MarkitectLiquibaseAutoConfiguration() {}

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(ConnectionCallback.class)
  @ConditionalOnMissingBean(SpringLiquibase.class)
  @EnableConfigurationProperties({LiquibaseProperties.class, MarkitectLiquibaseProperties.class})
  public static class MarkitectLiquibaseConfiguration {
    @Bean
    @ConditionalOnMissingBean(LiquibaseConnectionDetails.class)
    PropertiesLiquibaseConnectionDetails liquibaseConnectionDetails(
        LiquibaseProperties properties) {
      return new PropertiesLiquibaseConnectionDetails(properties);
    }

    @Bean
    SpringLiquibase liquibase(
        ObjectProvider<DataSource> dataSource,
        @LiquibaseDataSource @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
            ObjectProvider<DataSource> liquibaseDataSource,
        LiquibaseProperties properties,
        LiquibaseConnectionDetails connectionDetails,
        MarkitectLiquibaseProperties markitectProperties) {
      return toSpringLiquibase(
          dataSource.getIfUnique(),
          liquibaseDataSource.getIfAvailable(),
          properties,
          connectionDetails,
          markitectProperties);
    }

    private static SpringLiquibase toSpringLiquibase(
        @Nullable DataSource dataSource,
        @Nullable DataSource liquibaseDataSource,
        LiquibaseProperties properties,
        LiquibaseConnectionDetails connectionDetails,
        MarkitectLiquibaseProperties markitectProperties) {
      var migrationDataSource =
          toMigrationDataSource(liquibaseDataSource, dataSource, connectionDetails);
      var liquibase = new MarkitectSpringLiquibase();
      liquibase.setDropFirst(properties.isDropFirst());
      liquibase.setClearCheckSums(properties.isClearChecksums());
      liquibase.setShouldRun(properties.isEnabled());
      liquibase.setDataSource(migrationDataSource);
      liquibase.setChangeLog(properties.getChangeLog());
      liquibase.setContexts(properties.getContexts());
      liquibase.setLabelFilter(properties.getLabelFilter());
      liquibase.setTag(properties.getTag());
      liquibase.setDefaultSchema(properties.getDefaultSchema());
      liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
      liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
      liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
      liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
      liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
      liquibase.setChangeLogParameters(properties.getParameters());
      liquibase.setRollbackFile(properties.getRollbackFile());
      if (properties.getShowSummary() != null) {
        liquibase.setShowSummary(UpdateSummaryEnum.valueOf(properties.getShowSummary().name()));
      }
      if (properties.getShowSummaryOutput() != null) {
        liquibase.setShowSummaryOutput(
            UpdateSummaryOutputEnum.valueOf(properties.getShowSummaryOutput().name()));
      }
      liquibase.setOutputDefaultCatalog(markitectProperties.isOutputDefaultCatalog());
      liquibase.setOutputDefaultSchema(markitectProperties.isOutputDefaultSchema());
      return liquibase;
    }

    private static DataSource toMigrationDataSource(
        @Nullable DataSource liquibaseDataSource,
        @Nullable DataSource dataSource,
        LiquibaseConnectionDetails connectionDetails) {
      if (liquibaseDataSource != null) {
        return liquibaseDataSource;
      }
      @Nullable String url = connectionDetails.getJdbcUrl();
      if (url != null) {
        var builder = DataSourceBuilder.create();
        builder.type(SimpleDriverDataSource.class);
        builder.url(url);
        applyCommonBuilderProperties(connectionDetails, builder);
        return builder.build();
      }
      Assert.state(dataSource != null, "Liquibase migration DataSource missing");
      if (connectionDetails.getUsername() != null) {
        var builder = DataSourceBuilder.derivedFrom(dataSource);
        builder.type(SimpleDriverDataSource.class);
        applyCommonBuilderProperties(connectionDetails, builder);
        return builder.build();
      }
      return verifyNotNull(dataSource);
    }

    private static void applyCommonBuilderProperties(
        LiquibaseConnectionDetails connectionDetails, DataSourceBuilder<?> builder) {
      builder.username(connectionDetails.getUsername());
      builder.password(connectionDetails.getPassword());
      Optional.ofNullable(connectionDetails.getDriverClassName())
          .filter(StringUtils::hasText)
          .ifPresent(builder::driverClassName);
    }
  }

  static final class LiquibaseDataSourceCondition extends AnyNestedCondition {
    LiquibaseDataSourceCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(DataSource.class)
    @SuppressWarnings("unused")
    interface DataSourceBeanCondition {}

    @ConditionalOnBean(JdbcConnectionDetails.class)
    @SuppressWarnings("unused")
    private static final class JdbcConnectionDetailsCondition {}

    @ConditionalOnProperty(prefix = "spring.liquibase", name = "url")
    @SuppressWarnings("unused")
    interface LiquibaseUrlCondition {}
  }

  static class LiquibaseAutoConfigurationRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
      hints.resources().registerPattern("db/changelog/*");
    }
  }

  static final class PropertiesLiquibaseConnectionDetails implements LiquibaseConnectionDetails {
    private final LiquibaseProperties properties;

    PropertiesLiquibaseConnectionDetails(LiquibaseProperties properties) {
      checkNotNull(properties, "Properties must not be null");
      this.properties = properties;
    }

    @Override
    public @Nullable String getUsername() {
      return properties.getUser();
    }

    @Override
    public @Nullable String getPassword() {
      return properties.getPassword();
    }

    @Override
    public @Nullable String getJdbcUrl() {
      return properties.getUrl();
    }

    @Override
    public @Nullable String getDriverClassName() {
      return Optional.ofNullable(properties.getDriverClassName())
          .orElseGet(LiquibaseConnectionDetails.super::getDriverClassName);
    }
  }
}
