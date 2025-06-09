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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.function.Predicate.not;

import com.google.common.annotations.VisibleForTesting;
import dev.markitect.liquibase.spring.MarkitectSpringLiquibase;
import dev.markitect.liquibase.spring.SpringLiquibaseBeanPostProcessor;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseAutoConfigurationRuntimeHints;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseDataSourceCondition;
import java.util.Optional;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.change.DatabaseChange;
import liquibase.integration.spring.Customizer;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.ui.UIServiceEnum;
import org.jspecify.annotations.Nullable;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@AutoConfiguration(
    before = LiquibaseAutoConfiguration.class,
    after = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ConditionalOnClass({SpringLiquibase.class, DatabaseChange.class})
@ConditionalOnProperty(name = "spring.liquibase.enabled", matchIfMissing = true)
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
        @LiquibaseDataSource ObjectProvider<DataSource> liquibaseDataSource,
        LiquibaseProperties properties,
        ObjectProvider<SpringLiquibaseCustomizer> customizers,
        LiquibaseConnectionDetails connectionDetails,
        MarkitectLiquibaseProperties markitectProperties) {
      return toSpringLiquibase(
          dataSource.getIfUnique(),
          liquibaseDataSource.getIfAvailable(),
          properties,
          customizers,
          connectionDetails,
          markitectProperties);
    }

    private static SpringLiquibase toSpringLiquibase(
        @Nullable DataSource dataSource,
        @Nullable DataSource liquibaseDataSource,
        LiquibaseProperties properties,
        ObjectProvider<SpringLiquibaseCustomizer> customizers,
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
      Optional.ofNullable(properties.getContexts())
          .filter(not(CollectionUtils::isEmpty))
          .map(StringUtils::collectionToCommaDelimitedString)
          .ifPresent(liquibase::setContexts);
      Optional.ofNullable(properties.getLabelFilter())
          .filter(not(CollectionUtils::isEmpty))
          .map(StringUtils::collectionToCommaDelimitedString)
          .ifPresent(liquibase::setLabelFilter);
      liquibase.setTag(properties.getTag());
      liquibase.setDefaultSchema(properties.getDefaultSchema());
      liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
      liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
      liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
      liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
      liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
      liquibase.setChangeLogParameters(properties.getParameters());
      liquibase.setRollbackFile(properties.getRollbackFile());
      Optional.ofNullable(properties.getShowSummary())
          .map(Enum::name)
          .map(UpdateSummaryEnum::valueOf)
          .ifPresent(liquibase::setShowSummary);
      Optional.ofNullable(properties.getShowSummaryOutput())
          .map(Enum::name)
          .map(UpdateSummaryOutputEnum::valueOf)
          .ifPresent(liquibase::setShowSummaryOutput);
      Optional.ofNullable(properties.getUiService())
          .map(Enum::name)
          .map(UIServiceEnum::valueOf)
          .ifPresent(liquibase::setUiService);
      try {
        liquibase.setAnalyticsEnabled(properties.getAnalyticsEnabled());
      } catch (NoSuchMethodError ignore) {
        // Spring Boot 3.4 and earlier
      }
      try {
        liquibase.setLicenseKey(properties.getLicenseKey());
      } catch (NoSuchMethodError ignore) {
        // Spring Boot 3.4 and earlier
      }
      customizers.orderedStream().forEach(customizer -> customizer.customize(liquibase));
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
      String url = connectionDetails.getJdbcUrl();
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

  @ConditionalOnClass(Customizer.class)
  @SuppressWarnings("unused")
  static class CustomizerConfiguration {
    @Bean
    @ConditionalOnBean(Customizer.class)
    SpringLiquibaseCustomizer springLiquibaseCustomizer(Customizer<Liquibase> customizer) {
      return liquibase -> liquibase.setCustomizer(customizer);
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

    @ConditionalOnProperty(name = "spring.liquibase.url")
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

  @FunctionalInterface
  @VisibleForTesting
  interface SpringLiquibaseCustomizer {
    void customize(SpringLiquibase liquibase);
  }
}
