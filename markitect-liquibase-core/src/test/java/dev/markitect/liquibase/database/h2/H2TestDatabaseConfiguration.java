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

package dev.markitect.liquibase.database.h2;

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
public class H2TestDatabaseConfiguration {
  @Bean
  @Scope("prototype")
  @DependsOn("h2TestDataSource")
  public DatabaseBuilder<MarkitectH2Database> h2TestDatabaseBuilder(TestDatabaseSpecs specs) {
    String jdbcUrl = "jdbc:h2:mem:" + specs.getCatalogName() + ";DB_CLOSE_DELAY=-1";
    var databaseConnectionBuilder =
        DatabaseConnectionBuilder.newBuilder()
            .url(jdbcUrl)
            .username(specs.getUsername())
            .password(specs.getPassword())
            .driver("org.h2.Driver");
    return DatabaseBuilder.newBuilder(MarkitectH2Database.class)
        .databaseConnection(databaseConnectionBuilder);
  }

  @Bean
  @Lazy
  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  @SuppressWarnings({"resource", "SqlSourceToSinkFlow"})
  public DataSource h2TestDataSource(TestDatabaseSpecs specs) {
    var database = new MarkitectH2Database();
    String jdbcUrl = "jdbc:h2:mem:" + specs.getCatalogName() + ";DB_CLOSE_DELAY=-1";
    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(jdbcUrl);
    hikariConfig.setUsername(specs.getUsername());
    hikariConfig.setPassword(specs.getPassword());
    hikariConfig.setDriverClassName("org.h2.Driver");
    var dataSource = new HikariDataSource(hikariConfig);
    var jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(
        "CREATE SCHEMA " + database.escapeObjectName(specs.getAlternateSchemaName(), Schema.class));
    return dataSource;
  }
}
