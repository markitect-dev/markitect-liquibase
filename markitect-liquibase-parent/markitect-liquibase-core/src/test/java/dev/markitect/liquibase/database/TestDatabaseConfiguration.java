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

package dev.markitect.liquibase.database;

import dev.markitect.liquibase.database.h2.H2TestDatabaseConfiguration;
import dev.markitect.liquibase.database.hsqldb.HsqlDatabaseConfiguration;
import dev.markitect.liquibase.database.mssql.MssqlTestDatabaseConfiguration;
import dev.markitect.liquibase.database.postgresql.PostgresTestDatabaseConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import({
  H2TestDatabaseConfiguration.class,
  HsqlDatabaseConfiguration.class,
  MssqlTestDatabaseConfiguration.class,
  PostgresTestDatabaseConfiguration.class,
})
public class TestDatabaseConfiguration {
  @Bean
  @Primary
  TestDatabaseSpecs defaultTestDatabaseSpecs() {
    return TestDatabaseSpecs.builder()
        .setUsername("lbuser")
        .setPassword("LiquibasePass1")
        .setCatalogName("lbcat")
        .setAlternateCatalogName("lbcat2")
        .setAlternateSchemaName("lbschem2")
        .setAlternateTablespaceName("liquibase2")
        .build();
  }
}
