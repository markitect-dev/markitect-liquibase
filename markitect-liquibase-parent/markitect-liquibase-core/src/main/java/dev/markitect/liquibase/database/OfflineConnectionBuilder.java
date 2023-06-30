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
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import dev.markitect.liquibase.base.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import liquibase.Scope;

public final class OfflineConnectionBuilder {
  public static OfflineConnectionBuilder of(String shortName) {
    checkNotNull(shortName);
    return new OfflineConnectionBuilder(shortName, null, null, null, null, null, emptyMap());
  }

  private final String shortName;
  private final @Nullable String productName;
  private final @Nullable String version;
  private final @Nullable String snapshot;
  private final @Nullable String catalog;
  private final @Nullable String schema;
  private final Map<String, String> databaseParams;

  private OfflineConnectionBuilder(
      String shortName,
      @Nullable String productName,
      @Nullable String version,
      @Nullable String snapshot,
      @Nullable String catalog,
      @Nullable String schema,
      Map<String, String> databaseParams) {
    this.shortName = checkNotNull(shortName);
    this.productName = productName;
    this.version = version;
    this.snapshot = snapshot;
    this.catalog = catalog;
    this.schema = schema;
    this.databaseParams = checkNotNull(databaseParams);
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
    StringJoiner params = new StringJoiner("&", "?", "");
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
    MarkitectOfflineConnection connection =
        new MarkitectOfflineConnection(url, Scope.getCurrentScope().getResourceAccessor());
    connection.setCatalog(catalog);
    connection.setSchema(schema);
    return connection;
  }
}
