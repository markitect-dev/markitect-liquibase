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

package dev.markitect.liquibase.database.postgresql;

import static dev.markitect.liquibase.base.Verify.verifyNotNull;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.database.DatabaseConnectionBuilder;
import dev.markitect.liquibase.database.TestDatabaseSpecs;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class PostgresTestDatabaseConfiguration {
  @Bean
  @Scope("prototype")
  public DatabaseBuilder<MarkitectPostgresDatabase> postgresTestDatabaseBuilder(
      TestDatabaseSpecs specs, PostgreSQLContainer<?> container) {
    var databaseConnectionBuilder =
        DatabaseConnectionBuilder.newBuilder()
            .url(container.getJdbcUrl())
            .username(specs.getUsername())
            .password(specs.getPassword())
            .driver(container.getDriverClassName());
    return DatabaseBuilder.newBuilder(MarkitectPostgresDatabase.class)
        .databaseConnection(databaseConnectionBuilder);
  }

  @Bean
  @Lazy
  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  @SuppressWarnings("resource")
  public PostgreSQLContainer<?> postgresTestDatabaseContainer(TestDatabaseSpecs specs) {
    var database = new MarkitectPostgresDatabase();
    var container =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("15"))
            .withDatabaseName(specs.getCatalogName())
            .withUsername(specs.getUsername())
            .withPassword(specs.getPassword());
    container.start();
    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(container.getJdbcUrl());
    hikariConfig.setUsername(container.getUsername());
    hikariConfig.setPassword(container.getPassword());
    hikariConfig.setDriverClassName(container.getDriverClassName());
    try (var dataSource = new HikariDataSource(hikariConfig)) {
      var jdbcTemplate = new JdbcTemplate(dataSource);
      String dataDirectory =
          verifyNotNull(jdbcTemplate.queryForObject("SHOW data_directory", String.class));
      jdbcTemplate.execute(
          "COPY (SELECT 1) TO PROGRAM 'mkdir -p "
              + database.escapeStringForDatabase(
                  dataDirectory + "/" + specs.getAlternateTablespaceName())
              + "'");
      jdbcTemplate.execute(
          "CREATE TABLESPACE "
              + database.escapeObjectName(specs.getAlternateTablespaceName(), DatabaseObject.class)
              + " LOCATION '"
              + database.escapeStringForDatabase(
                  dataDirectory + "/" + specs.getAlternateTablespaceName())
              + "'");
      jdbcTemplate.execute(
          "CREATE SCHEMA "
              + database.escapeObjectName(specs.getAlternateSchemaName(), Schema.class));
      jdbcTemplate.execute(
          "CREATE DATABASE "
              + database.escapeObjectName(specs.getAlternateCatalogName(), Catalog.class));
    }
    container.withDatabaseName(specs.getAlternateCatalogName());
    String alternateJdbcUrl = container.getJdbcUrl();
    container.withDatabaseName(specs.getCatalogName());
    var alternateHikariConfig = new HikariConfig();
    alternateHikariConfig.setJdbcUrl(alternateJdbcUrl);
    alternateHikariConfig.setUsername(container.getUsername());
    alternateHikariConfig.setPassword(container.getPassword());
    alternateHikariConfig.setDriverClassName(container.getDriverClassName());
    try (var alternateDataSource = new HikariDataSource(alternateHikariConfig)) {
      var alternateJdbcTemplate = new JdbcTemplate(alternateDataSource);
      alternateJdbcTemplate.execute(
          "CREATE SCHEMA "
              + database.escapeObjectName(specs.getAlternateSchemaName(), Schema.class));
    }
    return container;
  }
}
