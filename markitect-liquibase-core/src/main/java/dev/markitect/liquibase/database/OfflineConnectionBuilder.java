/*
 * Copyright 2023-2026 Markitect
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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyMap;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.StringJoiner;
import liquibase.Scope;
import org.jspecify.annotations.Nullable;

/** Builds configured offline database connections. */
public final class OfflineConnectionBuilder {
  /** Creates a new offline connection builder. */
  public static OfflineConnectionBuilder newBuilder(String shortName) {
    return new OfflineConnectionBuilder(checkNotNull(shortName));
  }

  private final String shortName;
  private @Nullable String productName;
  private @Nullable String version;
  private @Nullable String snapshot;
  private @Nullable String catalog;
  private @Nullable String schema;
  private Map<String, String> databaseParams = emptyMap();

  private OfflineConnectionBuilder(String shortName) {
    this.shortName = checkNotNull(shortName);
  }

  /** Sets the database product name. */
  @CanIgnoreReturnValue
  public OfflineConnectionBuilder productName(@Nullable String productName) {
    this.productName = productName;
    return this;
  }

  /** Sets the database version. */
  @CanIgnoreReturnValue
  public OfflineConnectionBuilder version(@Nullable String version) {
    this.version = version;
    return this;
  }

  /** Sets the snapshot path. */
  @CanIgnoreReturnValue
  public OfflineConnectionBuilder snapshot(@Nullable String snapshot) {
    this.snapshot = snapshot;
    return this;
  }

  /** Sets the catalog name. */
  @CanIgnoreReturnValue
  public OfflineConnectionBuilder catalog(@Nullable String catalog) {
    this.catalog = catalog;
    return this;
  }

  /** Sets the schema name. */
  @CanIgnoreReturnValue
  public OfflineConnectionBuilder schema(@Nullable String schema) {
    this.schema = schema;
    return this;
  }

  /** Sets additional database parameters. */
  @CanIgnoreReturnValue
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public OfflineConnectionBuilder databaseParams(Map<String, String> databaseParams) {
    this.databaseParams = checkNotNull(databaseParams);
    return this;
  }

  /** Builds the configured offline connection. */
  public MarkitectOfflineConnection build() {
    var params = new StringJoiner("&", "?", "");
    if (productName != null) {
      params.add("productName=" + productName);
    }
    if (version != null) {
      params.add("version=" + version);
    }
    if (snapshot != null) {
      params.add("snapshot=" + snapshot);
    }
    if (catalog != null) {
      params.add("catalog=" + catalog);
    }
    databaseParams.forEach((key, value) -> params.add(key + "=" + value));
    String url = "offline:" + shortName + params;
    var connection =
        new MarkitectOfflineConnection(url, Scope.getCurrentScope().getResourceAccessor());
    connection.setCatalog(catalog);
    connection.setSchema(schema);
    return connection;
  }
}
