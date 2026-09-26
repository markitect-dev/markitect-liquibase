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

package dev.markitect.liquibase.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;

/** Provides helpers for Liquibase database object structures. */
public final class Structures {
  /** Determines whether a database object type is a catalog or schema type. */
  public static boolean isCatalogOrSchemaType(Class<? extends DatabaseObject> objectType) {
    checkNotNull(objectType);
    return objectType == Catalog.class || objectType == Schema.class;
  }

  private Structures() {}
}
