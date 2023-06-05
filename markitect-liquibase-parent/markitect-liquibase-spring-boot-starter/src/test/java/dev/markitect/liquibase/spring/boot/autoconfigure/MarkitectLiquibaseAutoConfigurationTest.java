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

import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseDataSourceCondition;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseDataSourceCondition.DataSourceBeanCondition;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.LiquibaseDataSourceCondition.LiquibaseUrlCondition;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfiguration.MarkitectLiquibaseConfiguration;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfigurationTest.ReferenceMarkitectLiquibaseAutoConfiguration.ReferenceMarkitectLiquibaseConfiguration;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfigurationTest.ReferenceMarkitectLiquibaseAutoConfiguration.ReferenceMarkitectLiquibaseConfiguration.ReferenceDataSourceBeanCondition;
import dev.markitect.liquibase.spring.boot.autoconfigure.MarkitectLiquibaseAutoConfigurationTest.ReferenceMarkitectLiquibaseAutoConfiguration.ReferenceMarkitectLiquibaseConfiguration.ReferenceLiquibaseUrlCondition;
import javax.sql.DataSource;
import liquibase.change.DatabaseChange;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.mock.env.MockEnvironment;

class MarkitectLiquibaseAutoConfigurationTest {
  @Test
  void test() {
    // given
    var environment = new MockEnvironment();
    var markitectLiquibaseAutoConfiguration = new MarkitectLiquibaseAutoConfiguration();
    var liquibaseDataSourceCondition = new LiquibaseDataSourceCondition();
    var dataSourceBeanCondition = new DataSourceBeanCondition();
    var liquibaseUrlCondition = new LiquibaseUrlCondition();

    // when/then
    assertThat(MarkitectLiquibaseAutoConfiguration.class.getAnnotations())
        .containsExactlyInAnyOrder(
            ReferenceMarkitectLiquibaseAutoConfiguration.class.getAnnotations());
    assertThat(MarkitectLiquibaseConfiguration.class.getAnnotations())
        .containsExactlyInAnyOrder(ReferenceMarkitectLiquibaseConfiguration.class.getAnnotations());
    assertThat(liquibaseDataSourceCondition.getConfigurationPhase())
        .isEqualTo(ConfigurationPhase.REGISTER_BEAN);
    assertThat(dataSourceBeanCondition.getClass().getAnnotations())
        .containsExactlyInAnyOrder(ReferenceDataSourceBeanCondition.class.getAnnotations());
    assertThat(liquibaseUrlCondition.getClass().getAnnotations())
        .containsExactlyInAnyOrder(ReferenceLiquibaseUrlCondition.class.getAnnotations());

    // when
    var springLiquibaseBeanPostProcessor =
        markitectLiquibaseAutoConfiguration.springLiquibaseBeanPostProcessor(environment);

    // then
    assertThat(springLiquibaseBeanPostProcessor).isNotNull();
  }

  @AutoConfiguration(
      before = LiquibaseAutoConfiguration.class,
      after = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
  @ConditionalOnClass({SpringLiquibase.class, DatabaseChange.class})
  @ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", matchIfMissing = true)
  @Conditional(LiquibaseDataSourceCondition.class)
  @Import(DatabaseInitializationDependencyConfigurer.class)
  interface ReferenceMarkitectLiquibaseAutoConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ConnectionCallback.class)
    @ConditionalOnMissingBean(SpringLiquibase.class)
    @EnableConfigurationProperties({LiquibaseProperties.class, MarkitectLiquibaseProperties.class})
    interface ReferenceMarkitectLiquibaseConfiguration {
      @ConditionalOnBean(DataSource.class)
      interface ReferenceDataSourceBeanCondition {}

      @ConditionalOnProperty(prefix = "spring.liquibase", name = "url")
      interface ReferenceLiquibaseUrlCondition {}
    }
  }
}
