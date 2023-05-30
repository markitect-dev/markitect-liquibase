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

class VerifySpec extends Specification {
  def verify() {
    when:
    Verify.verify(true)
    Verify.verify(true, 'errorMessage')

    then:
    noExceptionThrown()
  }

  def 'verify throws VerifyException'() {
    when:
    Verify.verify(false)

    then:
    thrown(VerifyException)

    when:
    Verify.verify(false, 'errorMessage')

    then:
    def thrown = thrown(VerifyException)
    thrown.message == 'errorMessage'
  }

  def verifyNotNull() {
    given:
    def reference = new Object()

    expect:
    Verify.verifyNotNull(reference) === reference
    Verify.verifyNotNull(reference, 'errorMessage') === reference
  }

  def 'verifyNotNull throws VerifyException'() {
    when:
    Verify.verifyNotNull(null)

    then:
    thrown(VerifyException)

    when:
    Verify.verifyNotNull(null, 'errorMessage')

    then:
    def thrown = thrown(VerifyException)
    thrown.message == 'errorMessage'
  }
}
