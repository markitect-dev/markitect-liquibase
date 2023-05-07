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

class PreconditionsSpec extends Specification {
  def checkIndex() {
    expect:
    Preconditions.checkIndex(index, size) == index
    Preconditions.checkIndex(index, size, 'errorMessage') == index

    where:
    index | size
    0     | 1
    1     | 2
  }

  def 'checkIndex throws IndexOutOfBoundsException'() {
    when:
    Preconditions.checkIndex(index, size)

    then:
    thrown(IndexOutOfBoundsException)

    when:
    Preconditions.checkIndex(index, size, 'errorMessage')

    then:
    def thrown = thrown(IndexOutOfBoundsException)
    thrown.message == 'errorMessage'

    where:
    index | size
    0     | 0
    1     | 1
  }

  def checkNotNull() {
    given:
    def reference = new Object()

    expect:
    Preconditions.checkNotNull(reference) === reference
    Preconditions.checkNotNull(reference, 'errorMessage') === reference
  }

  def 'checkNotNull throws NullPointerException'() {
    when:
    Preconditions.checkNotNull(null)

    then:
    thrown(NullPointerException)

    when:
    Preconditions.checkNotNull(null, 'errorMessage')

    then:
    def thrown = thrown(NullPointerException)
    thrown.message == 'errorMessage'
  }

  def checkState() {
    when:
    Preconditions.checkState(true)
    Preconditions.checkState(true, 'errorMessage')

    then:
    noExceptionThrown()
  }

  def 'checkState throws IllegalStateException'() {
    when:
    Preconditions.checkState(false)

    then:
    thrown(IllegalStateException)

    when:
    Preconditions.checkState(false, 'errorMessage')

    then:
    def thrown = thrown(IllegalStateException)
    thrown.message == 'errorMessage'
  }
}
