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

import dev.markitect.liquibase.spring.MarkitectSpringLiquibase;
import dev.markitect.liquibase.spring.SpringLiquibaseBeanPostProcessor;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseDataSourceCondition;
import java.util.Optional;
import javax.sql.DataSource;
import liquibase.change.DatabaseChange;
import liquibase.integration.spring.SpringLiquibase;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
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
public class MarkitectLiquibaseAutoConfiguration {
  @Bean
  public SpringLiquibaseBeanPostProcessor springLiquibaseBeanPostProcessor(
      Environment environment) {
    return new SpringLiquibaseBeanPostProcessor(environment);
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(ConnectionCallback.class)
  @ConditionalOnMissingBean(SpringLiquibase.class)
  @EnableConfigurationProperties({LiquibaseProperties.class, MarkitectLiquibaseProperties.class})
  public static class MarkitectLiquibaseConfiguration {
    private final LiquibaseProperties properties;
    private final MarkitectLiquibaseProperties markitectProperties;

    MarkitectLiquibaseConfiguration(
        LiquibaseProperties properties, MarkitectLiquibaseProperties markitectProperties) {
      Assert.notNull(properties, "Properties must not be null");
      Assert.notNull(markitectProperties, "MarkitectProperties must not be null");
      this.properties = properties;
      this.markitectProperties = markitectProperties;
    }

    @Bean
    SpringLiquibase liquibase(
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") @LiquibaseDataSource
            ObjectProvider<DataSource> liquibaseDataSource,
        ObjectProvider<DataSource> dataSource) {
      return toSpringLiquibase(liquibaseDataSource.getIfAvailable(), dataSource.getIfUnique());
    }

    private SpringLiquibase toSpringLiquibase(
        @Nullable DataSource liquibaseDataSource, @Nullable DataSource dataSource) {
      DataSource migrationDataSource = toMigrationDataSource(liquibaseDataSource, dataSource);
      MarkitectSpringLiquibase liquibase = new MarkitectSpringLiquibase();
      liquibase.setDropFirst(properties.isDropFirst());
      liquibase.setClearCheckSums(properties.isClearChecksums());
      liquibase.setShouldRun(properties.isEnabled());
      liquibase.setDataSource(migrationDataSource);
      liquibase.setChangeLog(properties.getChangeLog());
      liquibase.setContexts(properties.getContexts());
      liquibase.setLabelFilter(properties.getLabels());
      liquibase.setTag(properties.getTag());
      liquibase.setDefaultSchema(properties.getDefaultSchema());
      liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
      liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
      liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
      liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
      liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
      liquibase.setChangeLogParameters(properties.getParameters());
      liquibase.setRollbackFile(properties.getRollbackFile());
      liquibase.setCloseDataSourceOnceMigrated(
          migrationDataSource != liquibaseDataSource && migrationDataSource != dataSource);
      liquibase.setOutputDefaultCatalog(markitectProperties.getOutputDefaultCatalog());
      liquibase.setOutputDefaultSchema(markitectProperties.getOutputDefaultSchema());
      return liquibase;
    }

    private DataSource toMigrationDataSource(
        @Nullable DataSource liquibaseDataSource, @Nullable DataSource dataSource) {
      if (liquibaseDataSource != null) {
        return liquibaseDataSource;
      }
      String url = properties.getUrl();
      if (url != null) {
        DataSourceBuilder<?> builder = DataSourceBuilder.create();
        builder.type(SimpleDriverDataSource.class);
        builder.url(url);
        applyCommonBuilderProperties(builder);
        return builder.build();
      }
      Assert.state(dataSource != null, "Liquibase migration DataSource missing");
      if (properties.getUser() != null) {
        DataSourceBuilder<?> builder = DataSourceBuilder.derivedFrom(dataSource);
        builder.type(SimpleDriverDataSource.class);
        applyCommonBuilderProperties(builder);
        return builder.build();
      }
      return dataSource;
    }

    private void applyCommonBuilderProperties(DataSourceBuilder<?> builder) {
      Optional.ofNullable(properties.getDriverClassName())
          .filter(StringUtils::hasText)
          .ifPresent(builder::driverClassName);
      builder.username(properties.getUser());
      builder.password(properties.getPassword());
    }
  }

  static final class LiquibaseDataSourceCondition extends AnyNestedCondition {
    LiquibaseDataSourceCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    @SuppressWarnings("unused")
    @ConditionalOnBean(DataSource.class)
    static final class DataSourceBeanCondition {}

    @SuppressWarnings("unused")
    @ConditionalOnProperty(prefix = "spring.liquibase", name = "url")
    static final class LiquibaseUrlCondition {}
  }
}
