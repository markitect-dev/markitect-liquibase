/*
 * Copyright 2023-2024 Markitect
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

package dev.markitect.liquibase.statement;

import static org.assertj.core.api.Assertions.assertThat;

import dev.markitect.liquibase.database.DatabaseBuilder;
import dev.markitect.liquibase.database.h2.MarkitectH2Database;
import dev.markitect.liquibase.statement.InsertOrUpdateExecutablePreparedStatement.PreparedSql;
import java.util.ArrayList;
import liquibase.Scope;
import org.junit.jupiter.api.Test;

class InsertOrUpdateExecutablePreparedStatementTests {
  @Test
  void test() throws Exception {
    // given
    try (var database = DatabaseBuilder.newBuilder(MarkitectH2Database.class).build()) {

      // when
      var statement =
          new InsertOrUpdateExecutablePreparedStatement(
              database,
              null,
              null,
              null,
              new ArrayList<>(),
              null,
              Scope.getCurrentScope().getResourceAccessor(),
              new PreparedSql("", new ArrayList<>()));

      // then
      assertThat(statement.continueOnError()).isFalse();
    }
  }
}
