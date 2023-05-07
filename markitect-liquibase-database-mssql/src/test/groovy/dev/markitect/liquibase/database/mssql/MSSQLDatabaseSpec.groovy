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

package dev.markitect.liquibase.database.mssql

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS

import dev.markitect.liquibase.database.DatabaseBuilder
import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.Scope.ScopedRunnerWithReturn
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification

class MSSQLDatabaseSpec extends Specification {
  def correctObjectName() {
    when:
    def scopeValues = new LinkedHashMap<String, Object>().tap {
      if (preserveSchemaCase != null) {
        it[GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey()] = preserveSchemaCase
      }
      it
    }
    def database = DatabaseBuilder.of(MSSQLDatabase::new)
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .useOfflineConnection()
        .setObjectQuotingStrategy(quotingStrategy)
        .build()

    then:
    Scope.child(scopeValues, { database.correctObjectName(objectName, objectType) } as ScopedRunnerWithReturn<String>) == expected

    where:
    preserveSchemaCase | quotingStrategy   || objectName | objectType || expected
    null               | null              || null       | Table      || null
    null               | null              || 'Tbl1'     | Table      || 'Tbl1'
    null               | QUOTE_ALL_OBJECTS || 'Tbl1'     | Table      || 'Tbl1'
    null               | null              || 'Sch1'     | Schema     || 'Sch1'
    true               | null              || 'Sch1'     | Schema     || 'Sch1'
    null               | null              || 'Tbl 1'    | Table      || 'Tbl 1'
    null               | QUOTE_ALL_OBJECTS || 'Tbl 1'    | Table      || 'Tbl 1'
    null               | null              || 'Sch 1'    | Schema     || 'Sch 1'
    true               | null              || 'Sch 1'    | Schema     || 'Sch 1'
  }

  def escapeObjectName() {
    when:
    def scopeValues = new LinkedHashMap<String, Object>().tap {
      if (preserveSchemaCase != null) {
        it[GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey()] = preserveSchemaCase
      }
      it
    }
    def database = DatabaseBuilder.of(MSSQLDatabase::new)
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .useOfflineConnection()
        .setObjectQuotingStrategy(quotingStrategy)
        .build()

    then:
    Scope.child(scopeValues, { database.escapeObjectName(objectName, objectType) } as ScopedRunnerWithReturn<String>) == expected

    where:
    preserveSchemaCase | quotingStrategy   || objectName | objectType || expected
    null               | null              || null       | Table      || null
    null               | null              || 'Tbl1'     | Table      || 'Tbl1'
    null               | QUOTE_ALL_OBJECTS || 'Tbl1'     | Table      || '[Tbl1]'
    null               | null              || 'Sch1'     | Schema     || 'Sch1'
    true               | null              || 'Sch1'     | Schema     || '[Sch1]'
    null               | null              || 'Tbl 1'    | Table      || '[Tbl 1]'
    null               | QUOTE_ALL_OBJECTS || 'Tbl 1'    | Table      || '[Tbl 1]'
    null               | null              || 'Sch 1'    | Schema     || '[Sch 1]'
    true               | null              || 'Sch 1'    | Schema     || '[Sch 1]'
  }

  def escapeTableName() {
    when:
    def scopeValues = new LinkedHashMap<String, Object>().tap {
      if (includeCatalog != null) {
        it[GlobalConfiguration.INCLUDE_CATALOG_IN_SPECIFICATION.getKey()] = includeCatalog
      }
      it
    }
    def database = DatabaseBuilder.of(MSSQLDatabase::new)
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .setOutputDefaultCatalog(outputDefaultCatalog)
        .setOutputDefaultSchema(outputDefaultSchema)
        .useOfflineConnection(ocb -> ocb
            .setCatalog('Cat1')
            .setSchema('Sch1'))
        .build()

    then:
    database.defaultCatalogName == 'Cat1'
    database.defaultSchemaName == 'Sch1'
    Scope.child(scopeValues, { database.escapeTableName(catalogName, schemaName, tableName) } as ScopedRunnerWithReturn<String>) == expected

    where:
    includeCatalog | outputDefaultCatalog | outputDefaultSchema || catalogName | schemaName | tableName || expected
    null           | null                 | null                || null        | null       | 'Tbl1'    || 'Sch1.Tbl1'
    null           | null                 | null                || null        | 'Sch1'     | 'Tbl1'    || 'Sch1.Tbl1'
    null           | null                 | false               || null        | null       | 'Tbl1'    || 'Tbl1'
    null           | null                 | false               || null        | 'Sch1'     | 'Tbl1'    || 'Tbl1'
    true           | null                 | null                || null        | null       | 'Tbl1'    || 'Cat1.Sch1.Tbl1'
    true           | null                 | null                || null        | 'Sch1'     | 'Tbl1'    || 'Cat1.Sch1.Tbl1'
    true           | null                 | false               || null        | null       | 'Tbl1'    || 'Cat1..Tbl1'
    true           | null                 | false               || null        | 'Sch1'     | 'Tbl1'    || 'Cat1..Tbl1'
    null           | false                | null                || 'Cat2'      | null       | 'Tbl1'    || 'Cat2..Tbl1'
    null           | false                | null                || 'Cat2'      | 'Sch1'     | 'Tbl1'    || 'Cat2.Sch1.Tbl1'
  }
}
