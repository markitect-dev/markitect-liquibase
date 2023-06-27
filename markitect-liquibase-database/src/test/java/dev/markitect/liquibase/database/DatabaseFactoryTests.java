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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.Test;

class DatabaseFactoryTests {
  @Test
  void toDatabaseFactoryGet() throws Exception {
    // when
    var databaseFactory = DatabaseFactory.toDatabaseFactory(H2Database.class);
    var database = databaseFactory.get();

    // then
    assertThat(database).isInstanceOf(H2Database.class);
  }

  @Test
  void toDatabaseFactoryGetThrowsDatabaseException() {
    // when
    var databaseFactory = DatabaseFactory.toDatabaseFactory(Database.class);
    var thrown = catchThrowable(databaseFactory::get);

    // then
    assertThat(thrown).isInstanceOf(DatabaseException.class);
  }
}
