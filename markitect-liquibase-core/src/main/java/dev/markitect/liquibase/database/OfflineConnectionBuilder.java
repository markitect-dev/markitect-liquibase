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
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import liquibase.Scope;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class OfflineConnectionBuilder {
  private static final OfflineConnectionBuilder SINGLETON =
      new OfflineConnectionBuilder(null, null, null, null, null, null, emptyMap());

  public static OfflineConnectionBuilder of() {
    return SINGLETON;
  }

  private final @Nullable String shortName;
  private final @Nullable String productName;
  private final @Nullable String version;
  private final @Nullable String snapshot;
  private final @Nullable String catalog;
  private final @Nullable String schema;
  private final Map<String, String> databaseParams;

  private OfflineConnectionBuilder(
      @Nullable String shortName,
      @Nullable String productName,
      @Nullable String version,
      @Nullable String snapshot,
      @Nullable String catalog,
      @Nullable String schema,
      Map<String, String> databaseParams) {
    this.shortName = shortName;
    this.productName = productName;
    this.version = version;
    this.snapshot = snapshot;
    this.catalog = catalog;
    this.schema = schema;
    this.databaseParams = checkNotNull(databaseParams);
  }

  public OfflineConnectionBuilder withShortName(@Nullable String shortName) {
    return new OfflineConnectionBuilder(
        shortName, productName, version, snapshot, catalog, schema, databaseParams);
  }

  public OfflineConnectionBuilder withProductName(@Nullable String productName) {
    return new OfflineConnectionBuilder(
        shortName, productName, version, snapshot, catalog, schema, databaseParams);
  }

  public OfflineConnectionBuilder withVersion(@Nullable String version) {
    return new OfflineConnectionBuilder(
        shortName, productName, version, snapshot, catalog, schema, databaseParams);
  }

  public OfflineConnectionBuilder withSnapshot(@Nullable String snapshot) {
    return new OfflineConnectionBuilder(
        shortName, productName, version, snapshot, catalog, schema, databaseParams);
  }

  public OfflineConnectionBuilder withCatalog(@Nullable String catalog) {
    return new OfflineConnectionBuilder(
        shortName, productName, version, snapshot, catalog, schema, databaseParams);
  }

  public OfflineConnectionBuilder withSchema(@Nullable String schema) {
    return new OfflineConnectionBuilder(
        shortName, productName, version, snapshot, catalog, schema, databaseParams);
  }

  public OfflineConnectionBuilder withDatabaseParams(Map<String, String> databaseParams) {
    checkNotNull(databaseParams);
    return new OfflineConnectionBuilder(
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        unmodifiableMap(new LinkedHashMap<>(databaseParams)));
  }

  public MarkitectOfflineConnection build() {
    checkState(shortName != null);
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
