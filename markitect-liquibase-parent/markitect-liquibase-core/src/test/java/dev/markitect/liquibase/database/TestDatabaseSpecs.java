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

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;
import static dev.markitect.liquibase.base.Preconditions.checkState;
import static dev.markitect.liquibase.base.Verify.verifyNotNull;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TestDatabaseSpecs {
  public static Builder builder() {
    return new Builder();
  }

  private final String username;
  private final String password;
  private final String catalogName;
  private final String alternateCatalogName;
  private final String alternateSchemaName;
  private final String alternateTablespaceName;

  private TestDatabaseSpecs(
      String username,
      String password,
      String catalogName,
      String alternateCatalogName,
      String alternateSchemaName,
      String alternateTablespaceName) {
    this.username = checkNotNull(username);
    this.password = checkNotNull(password);
    this.catalogName = checkNotNull(catalogName);
    this.alternateCatalogName = checkNotNull(alternateCatalogName);
    this.alternateSchemaName = checkNotNull(alternateSchemaName);
    this.alternateTablespaceName = checkNotNull(alternateTablespaceName);
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public String getAlternateCatalogName() {
    return alternateCatalogName;
  }

  public String getAlternateSchemaName() {
    return alternateSchemaName;
  }

  public String getAlternateTablespaceName() {
    return alternateTablespaceName;
  }

  public static final class Builder {
    private @Nullable String username;
    private @Nullable String password;
    private @Nullable String catalogName;
    private @Nullable String alternateCatalogName;
    private @Nullable String alternateSchemaName;
    private @Nullable String alternateTablespaceName;

    private Builder() {}

    @CanIgnoreReturnValue
    public Builder setUsername(String username) {
      this.username = checkNotNull(username);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder setPassword(String password) {
      this.password = checkNotNull(password);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder setCatalogName(String catalogName) {
      this.catalogName = checkNotNull(catalogName);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder setAlternateCatalogName(String alternateCatalogName) {
      this.alternateCatalogName = checkNotNull(alternateCatalogName);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder setAlternateSchemaName(String alternateSchemaName) {
      this.alternateSchemaName = checkNotNull(alternateSchemaName);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder setAlternateTablespaceName(String alternateTablespaceName) {
      this.alternateTablespaceName = checkNotNull(alternateTablespaceName);
      return this;
    }

    public TestDatabaseSpecs build() {
      checkState(username != null);
      checkState(password != null);
      checkState(catalogName != null);
      checkState(alternateCatalogName != null);
      checkState(alternateSchemaName != null);
      checkState(alternateTablespaceName != null);
      return new TestDatabaseSpecs(
          verifyNotNull(username),
          verifyNotNull(password),
          verifyNotNull(catalogName),
          verifyNotNull(alternateCatalogName),
          verifyNotNull(alternateSchemaName),
          verifyNotNull(alternateTablespaceName));
    }
  }
}
