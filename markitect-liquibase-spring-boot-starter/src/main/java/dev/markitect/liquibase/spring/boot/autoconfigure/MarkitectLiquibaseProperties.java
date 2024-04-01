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

package dev.markitect.liquibase.spring.boot.autoconfigure;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "markitect.liquibase")
public class MarkitectLiquibaseProperties {
  /** Whether to qualify the names of objects in the default catalog. */
  private boolean outputDefaultCatalog;

  /** Whether to qualify the names of objects in the default schema. */
  private boolean outputDefaultSchema;

  /** Additional properties used to configure Liquibase. */
  private final Map<String, String> properties = new HashMap<>();

  public boolean isOutputDefaultCatalog() {
    return outputDefaultCatalog;
  }

  @CanIgnoreReturnValue
  public MarkitectLiquibaseProperties setOutputDefaultCatalog(boolean outputDefaultCatalog) {
    this.outputDefaultCatalog = outputDefaultCatalog;
    return this;
  }

  public boolean isOutputDefaultSchema() {
    return outputDefaultSchema;
  }

  @CanIgnoreReturnValue
  public MarkitectLiquibaseProperties setOutputDefaultSchema(boolean outputDefaultSchema) {
    this.outputDefaultSchema = outputDefaultSchema;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }
}
