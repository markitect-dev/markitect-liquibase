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

import static dev.markitect.liquibase.util.Preconditions.checkNotNull;
import static dev.markitect.liquibase.util.Preconditions.checkState;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import liquibase.resource.ResourceAccessor;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class OfflineConnectionBuilder {
  private static final OfflineConnectionBuilder SINGLETON =
      new OfflineConnectionBuilder(null, null, null, null, null, null, null, emptyMap());

  public static OfflineConnectionBuilder of() {
    return SINGLETON;
  }

  private final @Nullable ResourceAccessor resourceAccessor;
  private final @Nullable String shortName;
  private final @Nullable String productName;
  private final @Nullable String version;
  private final @Nullable String snapshot;
  private final @Nullable String catalog;
  private final @Nullable String schema;
  private final Map<String, String> databaseParams;

  private OfflineConnectionBuilder(
      @Nullable ResourceAccessor resourceAccessor,
      @Nullable String shortName,
      @Nullable String productName,
      @Nullable String version,
      @Nullable String snapshot,
      @Nullable String catalog,
      @Nullable String schema,
      Map<String, String> databaseParams) {
    this.resourceAccessor = resourceAccessor;
    this.schema = schema;
    this.shortName = shortName;
    this.productName = productName;
    this.version = version;
    this.snapshot = snapshot;
    this.catalog = catalog;
    this.databaseParams = unmodifiableMap(new HashMap<>(checkNotNull(databaseParams)));
  }

  public OfflineConnectionBuilder setResourceAccessor(@Nullable ResourceAccessor resourceAccessor) {
    return new OfflineConnectionBuilder(
        resourceAccessor,
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        databaseParams);
  }

  public OfflineConnectionBuilder setShortName(@Nullable String shortName) {
    return new OfflineConnectionBuilder(
        resourceAccessor,
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        databaseParams);
  }

  public OfflineConnectionBuilder setProductName(@Nullable String productName) {
    return new OfflineConnectionBuilder(
        resourceAccessor,
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        databaseParams);
  }

  public OfflineConnectionBuilder setVersion(@Nullable String version) {
    return new OfflineConnectionBuilder(
        resourceAccessor,
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        databaseParams);
  }

  public OfflineConnectionBuilder setSnapshot(@Nullable String snapshot) {
    return new OfflineConnectionBuilder(
        resourceAccessor,
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        databaseParams);
  }

  public OfflineConnectionBuilder setCatalog(@Nullable String catalog) {
    return new OfflineConnectionBuilder(
        resourceAccessor,
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        databaseParams);
  }

  public OfflineConnectionBuilder setSchema(@Nullable String schema) {
    return new OfflineConnectionBuilder(
        resourceAccessor,
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        databaseParams);
  }

  public OfflineConnectionBuilder setDatabaseParams(Map<String, String> databaseParams) {
    return new OfflineConnectionBuilder(
        resourceAccessor,
        shortName,
        productName,
        version,
        snapshot,
        catalog,
        schema,
        databaseParams);
  }

  public MarkitectOfflineConnection build() {
    checkState(resourceAccessor != null);
    checkState(shortName != null);
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
    this.databaseParams.forEach((key, value) -> params.add(key + "=" + value));
    String url = "offline:" + shortName + params;
    MarkitectOfflineConnection connection = new MarkitectOfflineConnection(url, resourceAccessor);
    connection.setSchema(schema);
    return connection;
  }
}
