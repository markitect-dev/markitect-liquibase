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

package dev.markitect.liquibase.structure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import liquibase.structure.DatabaseObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StructuresTests {
  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          # objectType                              | expected
          liquibase.structure.core.Data             | false
          liquibase.structure.core.PrimaryKey       | false
          liquibase.structure.core.Schema           | true
          liquibase.structure.core.UniqueConstraint | false
          liquibase.structure.core.Column           | false
          liquibase.structure.core.Index            | false
          liquibase.structure.core.View             | false
          liquibase.structure.core.Table            | false
          liquibase.structure.core.ForeignKey       | false
          liquibase.structure.core.StoredProcedure  | false
          liquibase.structure.core.Catalog          | true
          liquibase.structure.core.Sequence         | false
          """,
      delimiter = '|')
  void isCatalogOrSchemaType(Class<? extends DatabaseObject> objectType, boolean expected) {
    // when
    boolean actual = Structures.isCatalogOrSchemaType(objectType);

    // then
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SuppressWarnings("NullAway")
  void isCatalogOrSchemaTypeThrowsNullPointerException() {
    // when
    var thrown = catchThrowable(() -> Structures.isCatalogOrSchemaType(null));

    // then
    assertThat(thrown).isInstanceOf(NullPointerException.class);
  }
}
