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

package dev.markitect.liquibase.database.mssql;

import static com.google.common.base.Verify.verifyNotNull;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.database.DatabaseConnectionBuilder;
import dev.markitect.liquibase.database.TestDatabaseSpecs;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.mssqlserver.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class MssqlTestDatabaseConfiguration {
  @Bean
  @Scope("prototype")
  public DatabaseBuilder<MarkitectMssqlDatabase> mssqlTestDatabaseBuilder(
      TestDatabaseSpecs specs, MSSQLServerContainer container) {
    var databaseConnectionBuilder =
        DatabaseConnectionBuilder.newBuilder()
            .url(container.getJdbcUrl() + ";databaseName=" + specs.getCatalogName())
            .username(specs.getUsername())
            .password(specs.getPassword())
            .driver(container.getDriverClassName());
    return DatabaseBuilder.newBuilder(MarkitectMssqlDatabase.class)
        .databaseConnection(databaseConnectionBuilder);
  }

  @Bean
  @Lazy
  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  @SuppressWarnings({"resource", "SqlSourceToSinkFlow"})
  public MSSQLServerContainer mssqlTestDatabaseContainer(TestDatabaseSpecs specs) {
    var database = new MarkitectMssqlDatabase();
    var container =
        new MSSQLServerContainer(
                DockerImageName.parse("mcr.microsoft.com/mssql/server").withTag("2022-latest"))
            .acceptLicense();
    container.start();
    var masterHikariConfig = new HikariConfig();
    masterHikariConfig.setJdbcUrl(container.getJdbcUrl());
    masterHikariConfig.setUsername(container.getUsername());
    masterHikariConfig.setPassword(container.getPassword());
    masterHikariConfig.setDriverClassName(container.getDriverClassName());
    try (var masterDataSource = new HikariDataSource(masterHikariConfig)) {
      var masterJdbcTemplate = new JdbcTemplate(masterDataSource);
      String dataPath =
          verifyNotNull(
              masterJdbcTemplate.queryForObject(
                  "SELECT CAST(SERVERPROPERTY('InstanceDefaultDataPath') AS nvarchar(128))",
                  String.class));
      masterJdbcTemplate.execute(
          "CREATE LOGIN "
              + database.escapeObjectName(specs.getUsername(), DatabaseObject.class)
              + " WITH PASSWORD = N'"
              + database.escapeStringForDatabase(specs.getPassword())
              + "'");
      masterJdbcTemplate.execute(
          "ALTER SERVER ROLE sysadmin ADD MEMBER "
              + database.escapeObjectName(specs.getUsername(), DatabaseObject.class));
      for (String catalogName : List.of(specs.getCatalogName(), specs.getAlternateCatalogName())) {
        masterJdbcTemplate.execute(
            "CREATE DATABASE " + database.escapeObjectName(catalogName, Catalog.class));
        masterJdbcTemplate.execute(
            "ALTER DATABASE "
                + database.escapeObjectName(catalogName, Catalog.class)
                + " ADD FILEGROUP "
                + database.escapeObjectName(
                    specs.getAlternateTablespaceName(), DatabaseObject.class));
        masterJdbcTemplate.execute(
            "ALTER DATABASE "
                + database.escapeObjectName(catalogName, Catalog.class)
                + " ADD FILE (NAME = N'"
                + database.escapeStringForDatabase(specs.getAlternateTablespaceName())
                + "', FILENAME = N'"
                + database.escapeStringForDatabase(
                    dataPath + catalogName + "_" + specs.getAlternateTablespaceName() + ".ndf")
                + "') TO FILEGROUP "
                + database.escapeObjectName(
                    specs.getAlternateTablespaceName(), DatabaseObject.class));
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(container.getJdbcUrl() + ";databaseName=" + catalogName);
        hikariConfig.setUsername(container.getUsername());
        hikariConfig.setPassword(container.getPassword());
        hikariConfig.setDriverClassName(container.getDriverClassName());
        try (var dataSource = new HikariDataSource(hikariConfig)) {
          var jdbcTemplate = new JdbcTemplate(dataSource);
          jdbcTemplate.execute(
              "CREATE SCHEMA "
                  + database.escapeObjectName(specs.getAlternateSchemaName(), Schema.class));
        }
      }
    }
    return container;
  }
}
