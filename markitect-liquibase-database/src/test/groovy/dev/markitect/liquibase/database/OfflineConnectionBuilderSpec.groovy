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

package dev.markitect.liquibase.database

import liquibase.resource.ClassLoaderResourceAccessor
import spock.lang.Specification

class OfflineConnectionBuilderSpec extends Specification {
  def build() {
    given:
    def builder = OfflineConnectionBuilder.of()
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .setShortName(shortName)
        .setProductName(productName)
        .setVersion(version)
        .setSnapshot(snapshot)
        .setDatabaseParams(databaseParams)

    when:
    def connection = builder.build()

    then:
    connection.databaseProductName == expectedProductName
    connection.databaseProductVersion == productVersion
    connection.databaseMajorVersion == majorVersion
    connection.databaseMinorVersion == minorVersion
    (liquibase.database.OfflineConnection.metaClass.getProperty(connection, 'snapshot') == null) == (snapshot == null)
    liquibase.database.OfflineConnection.metaClass.getProperty(connection, 'databaseParams') == databaseParams

    where:
    shortName    | productName            | version      | snapshot                        | databaseParams                 || expectedProductName    | productVersion | majorVersion | minorVersion
    'h2'         | null                   | null         | null                            | [:]                            || 'Offline h2'           | null           | 999          | 999
    'h2'         | null                   | '1.4.200'    | null                            | [:]                            || 'Offline h2'           | '1.4.200'      | 1            | 4
    'mssql'      | null                   | null         | null                            | [:]                            || 'Offline mssql'        | null           | 999          | 999
    'mssql'      | null                   | null         | 'snapshots/snapshot-mssql.json' | [:]                            || 'Offline mssql'        | '16.00.4025'   | 999          | 999
    'mssql'      | 'Microsoft SQL Server' | '16.00.4025' | 'snapshots/snapshot-mssql.json' | [:]                            || 'Microsoft SQL Server' | '16.00.4025'   | 16           | 0
    'mssql'      | null                   | null         | null                            | ['defaultCatalogName': 'Cat1'] || 'Offline mssql'        | null           | 999          | 999
    'postgresql' | null                   | null         | null                            | [:]                            || 'Offline postgresql'   | null           | 999          | 999
  }

  def 'build fails'() {
    given:
    def builder = OfflineConnectionBuilder.of()
        .setResourceAccessor(resourceAccessor)
        .setShortName(shortName)

    when:
    builder.build()

    then:
    thrown(IllegalStateException)

    where:
    resourceAccessor                  | shortName
    new ClassLoaderResourceAccessor() | null
    null                              | 'h2'
  }
}
