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

package dev.markitect.liquibase.database.postgresql

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS

import dev.markitect.liquibase.database.DatabaseBuilder
import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.Scope.ScopedRunnerWithReturn
import liquibase.database.core.PostgresDatabase
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification

class PostgresDatabaseSpec extends Specification {
  def correctObjectName() {
    when:
    def scopeValues = new LinkedHashMap<String, Object>().tap {
      if (preserveSchemaCase != null) {
        it[GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey()] = preserveSchemaCase
      }
      it
    }
    def database = DatabaseBuilder.of(PostgresDatabase::new)
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
    def database = DatabaseBuilder.of(PostgresDatabase::new)
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .useOfflineConnection()
        .setObjectQuotingStrategy(quotingStrategy)
        .build()

    then:
    Scope.child(scopeValues, { database.escapeObjectName(objectName, objectType) } as ScopedRunnerWithReturn<String>) == expected

    where:
    preserveSchemaCase | quotingStrategy   || objectName | objectType || expected
    null               | null              || null       | Table      || null
    null               | null              || 'Tbl1'     | Table      || '"Tbl1"'
    null               | QUOTE_ALL_OBJECTS || 'Tbl1'     | Table      || '"Tbl1"'
    null               | null              || 'Sch1'     | Schema     || '"Sch1"'
    true               | null              || 'sch1'     | Schema     || '"sch1"'
    true               | null              || 'Sch1'     | Schema     || '"Sch1"'
    null               | null              || 'Tbl 1'    | Table      || '"Tbl 1"'
    null               | QUOTE_ALL_OBJECTS || 'Tbl 1'    | Table      || '"Tbl 1"'
    null               | null              || 'Sch 1'    | Schema     || '"Sch 1"'
    true               | null              || 'Sch 1'    | Schema     || '"Sch 1"'
  }

  def escapeTableName() {
    when:
    def database = DatabaseBuilder.of(PostgresDatabase::new)
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .setOutputDefaultSchema(outputDefaultSchema)
        .useOfflineConnection(ocb ->
            ocb.setSchema('PUBLIC'))
        .build()

    then:
    database.defaultSchemaName == 'PUBLIC'
    database.escapeTableName(catalogName, schemaName, tableName) == expected

    where:
    outputDefaultSchema || catalogName | schemaName | tableName || expected
    null                || null        | null       | 'Tbl1'    || 'PUBLIC."Tbl1"'
    null                || null        | 'PUBLIC'   | 'Tbl1'    || 'PUBLIC."Tbl1"'
    false               || null        | null       | 'Tbl1'    || '"Tbl1"'
    false               || null        | 'PUBLIC'   | 'Tbl1'    || '"Tbl1"'
  }
}
