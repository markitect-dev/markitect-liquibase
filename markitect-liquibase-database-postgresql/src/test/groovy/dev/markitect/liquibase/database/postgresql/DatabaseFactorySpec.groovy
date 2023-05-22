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

import dev.markitect.liquibase.database.OfflineConnectionBuilder
import liquibase.database.DatabaseFactory
import liquibase.resource.ClassLoaderResourceAccessor
import spock.lang.Specification

class DatabaseFactorySpec extends Specification {
  def findCorrectDatabaseImplementation() {
    given:
    def connection = OfflineConnectionBuilder.of()
        .setResourceAccessor(new ClassLoaderResourceAccessor())
        .setShortName(shortName)
        .build()

    when:
    def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)

    then:
    database.shortName == shortName
    database.class == expectedType

    where:
    shortName    || expectedType
    'postgresql' || MarkitectPostgresDatabase
  }
}
