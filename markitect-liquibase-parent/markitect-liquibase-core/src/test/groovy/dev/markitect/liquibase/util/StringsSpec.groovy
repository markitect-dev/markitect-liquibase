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

package dev.markitect.liquibase.util

import spock.lang.Specification

class StringsSpec extends Specification {
  def isIllegalIdentifier() {
    expect:
    Strings.isIllegalIdentifier(objectName) == expected

    where:
    objectName || expected
    'abc_123'  || false
    'Abc_123'  || false
    'ABC_123'  || false
    'abc 123'  || true
    'abc-123'  || true
    '123_abc'  || true
  }

  def 'isIllegalIdentifier throws NullPointerException'() {
    when:
    Strings.isIllegalIdentifier(null)

    then:
    thrown(NullPointerException)
  }
}
