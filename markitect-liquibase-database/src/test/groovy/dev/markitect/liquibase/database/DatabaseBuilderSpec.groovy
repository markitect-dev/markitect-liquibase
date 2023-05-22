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

import dev.markitect.liquibase.util.VerifyException
import liquibase.database.ObjectQuotingStrategy
import liquibase.database.core.H2Database
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.resource.ClassLoaderResourceAccessor
import spock.lang.Specification

class DatabaseBuilderSpec extends Specification {
  def build() {
    given:
    def builder = DatabaseBuilder.of(() -> databaseClass.getDeclaredConstructor().newInstance())
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .setObjectQuotingStrategy(quotingStrategy)
        .setOutputDefaultCatalog(outputDefaultCatalog)
        .setOutputDefaultSchema(outputDefaultSchema)
        .with {
          useOfflineConnection
              ? it.useOfflineConnection(ocb -> ocb.setVersion(version))
              : it
        }

    when:
    def database = builder.build()

    then:
    database.class == databaseClass
    database.shortName == shortName
    database.databaseProductName == productName
    database.databaseProductVersion == version
    database.databaseMajorVersion == majorVersion
    database.databaseMinorVersion == minorVersion
    database.objectQuotingStrategy == Optional.ofNullable(quotingStrategy).orElse(ObjectQuotingStrategy.LEGACY)
    database.outputDefaultCatalog == Optional.ofNullable(outputDefaultCatalog).orElse(true)
    database.outputDefaultSchema == Optional.ofNullable(outputDefaultSchema).orElse(true)
    useOfflineConnection ? database.connection && database.connection.class === MarkitectOfflineConnection : !database.connection

    when:
    builder = builder.setDatabaseFactory(MSSQLDatabase::new)
        .useOfflineConnection()
    database = builder.build()

    then:
    database.class == MSSQLDatabase
    database.shortName == 'mssql'
    database.databaseProductName == 'Offline mssql'
    database.databaseProductVersion == null
    database.databaseMajorVersion == 999
    database.databaseMinorVersion == 999
    database.objectQuotingStrategy == Optional.ofNullable(quotingStrategy).orElse(ObjectQuotingStrategy.LEGACY)
    database.outputDefaultCatalog == Optional.ofNullable(outputDefaultCatalog).orElse(true)
    database.outputDefaultSchema == Optional.ofNullable(outputDefaultSchema).orElse(true)
    database.connection && database.connection.class === MarkitectOfflineConnection

    where:
    databaseClass    | quotingStrategy                         | outputDefaultCatalog | outputDefaultSchema | useOfflineConnection | version   || shortName    | productName          | majorVersion | minorVersion
    H2Database       | null                                    | null                 | null                | false                | null      || 'h2'         | 'H2'                 | 999          | -1
    H2Database       | null                                    | null                 | null                | true                 | null      || 'h2'         | 'Offline h2'         | 999          | 999
    H2Database       | null                                    | null                 | null                | true                 | '1.4.200' || 'h2'         | 'Offline h2'         | 1            | 4
    H2Database       | null                                    | false                | false               | false                | null      || 'h2'         | 'H2'                 | 999          | -1
    H2Database       | ObjectQuotingStrategy.QUOTE_ALL_OBJECTS | false                | true                | false                | null      || 'h2'         | 'H2'                 | 999          | -1
    PostgresDatabase | null                                    | null                 | null                | false                | null      || 'postgresql' | 'PostgreSQL'         | 999          | -1
    PostgresDatabase | null                                    | null                 | null                | true                 | null      || 'postgresql' | 'Offline postgresql' | 999          | 999
  }

  def 'build with invalid database factory fails'() {
    given:
    def invalidBuilder = DatabaseBuilder.of(() -> null)

    when:
    invalidBuilder.build()

    then:
    thrown(VerifyException)
  }

  def 'build with invalid offline connection customizer fails'() {
    given:
    def invalidBuilder = DatabaseBuilder.of(H2Database::new)
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .useOfflineConnection(ocb -> null)

    when:
    invalidBuilder.build()

    then:
    thrown(VerifyException)
  }
}
