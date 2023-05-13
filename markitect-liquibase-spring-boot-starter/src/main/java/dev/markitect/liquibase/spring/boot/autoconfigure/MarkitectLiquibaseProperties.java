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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "markitect.liquibase")
public class MarkitectLiquibaseProperties {
  /** Whether to qualify the names of objects in the default catalog. */
  private volatile @Nullable Boolean outputDefaultCatalog = null;

  /** Whether to qualify the names of objects in the default schema. */
  private volatile @Nullable Boolean outputDefaultSchema = null;

  /** Whether to use ThreadLocalScopeManager instead of SingletonScopeManager. */
  private volatile boolean useThreadLocalScopeManager = false;

  /** Additional properties used to configure Liquibase. */
  private final Map<String, String> properties = new HashMap<>();

  public @Nullable Boolean getOutputDefaultCatalog() {
    return outputDefaultCatalog;
  }

  public MarkitectLiquibaseProperties setOutputDefaultCatalog(
      @Nullable Boolean outputDefaultCatalog) {
    this.outputDefaultCatalog = outputDefaultCatalog;
    return this;
  }

  public @Nullable Boolean getOutputDefaultSchema() {
    return outputDefaultSchema;
  }

  public MarkitectLiquibaseProperties setOutputDefaultSchema(
      @Nullable Boolean outputDefaultSchema) {
    this.outputDefaultSchema = outputDefaultSchema;
    return this;
  }

  public boolean isUseThreadLocalScopeManager() {
    return useThreadLocalScopeManager;
  }

  @CanIgnoreReturnValue
  public MarkitectLiquibaseProperties setUseThreadLocalScopeManager(
      boolean useThreadLocalScopeManager) {
    this.useThreadLocalScopeManager = useThreadLocalScopeManager;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }
}
