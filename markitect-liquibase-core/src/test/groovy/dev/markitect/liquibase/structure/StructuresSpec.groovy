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

package dev.markitect.liquibase.structure

import liquibase.structure.core.Catalog
import liquibase.structure.core.Column
import liquibase.structure.core.Data
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Index
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Schema
import liquibase.structure.core.Sequence
import liquibase.structure.core.StoredProcedure
import liquibase.structure.core.Table
import liquibase.structure.core.UniqueConstraint
import liquibase.structure.core.View
import spock.lang.Specification

class StructuresSpec extends Specification {
  def isCatalogOrSchemaType() {
    expect:
    Structures.isCatalogOrSchemaType(objectType) == expected

    where:
    objectType       || expected
    Data             || false
    PrimaryKey       || false
    Schema           || true
    UniqueConstraint || false
    Column           || false
    Index            || false
    View             || false
    Table            || false
    ForeignKey       || false
    StoredProcedure  || false
    Catalog          || true
    Sequence         || false
  }

  def 'isCatalogOrSchemaType throws NullPointerException'() {
    when:
    Structures.isCatalogOrSchemaType(null)

    then:
    thrown(NullPointerException)
  }
}
