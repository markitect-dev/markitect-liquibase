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

package dev.markitect.liquibase.database.hsqldb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.database.DatabaseConnectionBuilder;
import dev.markitect.liquibase.database.TestDatabaseSpecs;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.sql.DataSource;
import liquibase.structure.core.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class HsqlTestDatabaseConfiguration {
  @Bean
  @Scope("prototype")
  @DependsOn("hsqlTestDataSource")
  public DatabaseBuilder<MarkitectHsqlDatabase> hsqlTestDatabaseBuilder(TestDatabaseSpecs specs) {
    String jdbcUrl = "jdbc:hsqldb:mem:" + specs.getCatalogName();
    var databaseConnectionBuilder =
        DatabaseConnectionBuilder.newBuilder()
            .url(jdbcUrl)
            .username(specs.getUsername())
            .password(specs.getPassword())
            .driver("org.hsqldb.jdbc.JDBCDriver");
    return DatabaseBuilder.newBuilder(MarkitectHsqlDatabase.class)
        .databaseConnection(databaseConnectionBuilder);
  }

  @Bean
  @Lazy
  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  @SuppressWarnings({"resource", "SqlSourceToSinkFlow"})
  public DataSource hsqlTestDataSource(TestDatabaseSpecs specs) {
    var database = new MarkitectHsqlDatabase();
    String jdbcUrl = "jdbc:hsqldb:mem:" + specs.getCatalogName();
    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(jdbcUrl);
    hikariConfig.setUsername(specs.getUsername());
    hikariConfig.setPassword(specs.getPassword());
    hikariConfig.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
    var dataSource = new HikariDataSource(hikariConfig);
    var jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(
        "CREATE SCHEMA " + database.escapeObjectName(specs.getAlternateSchemaName(), Schema.class));
    return dataSource;
  }
}
