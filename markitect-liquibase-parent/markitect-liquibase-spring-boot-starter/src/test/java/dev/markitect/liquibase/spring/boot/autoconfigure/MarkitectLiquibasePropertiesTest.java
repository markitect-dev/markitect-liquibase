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

import java.util.Map;
import org.junit.jupiter.api.Test;

class MarkitectLiquibasePropertiesTest {
  @Test
  void test() {
    // when
    var markitectLiquibaseProperties = new MarkitectLiquibaseProperties();

    // then
    assertThat(markitectLiquibaseProperties)
        .extracting(
            MarkitectLiquibaseProperties::isOutputDefaultCatalog,
            MarkitectLiquibaseProperties::isOutputDefaultSchema,
            MarkitectLiquibaseProperties::isUseThreadLocalScopeManager,
            MarkitectLiquibaseProperties::getProperties)
        .containsExactly(false, false, false, Map.of());

    // when
    markitectLiquibaseProperties.setOutputDefaultCatalog(true);
    markitectLiquibaseProperties.setOutputDefaultSchema(true);
    markitectLiquibaseProperties.setUseThreadLocalScopeManager(true);
    markitectLiquibaseProperties.getProperties().put("liquibase.sql.logLevel", "info");

    // then
    assertThat(markitectLiquibaseProperties)
        .extracting(
            MarkitectLiquibaseProperties::isOutputDefaultCatalog,
            MarkitectLiquibaseProperties::isOutputDefaultSchema,
            MarkitectLiquibaseProperties::isUseThreadLocalScopeManager,
            MarkitectLiquibaseProperties::getProperties)
        .containsExactly(true, true, true, Map.of("liquibase.sql.logLevel", "info"));
  }
}
