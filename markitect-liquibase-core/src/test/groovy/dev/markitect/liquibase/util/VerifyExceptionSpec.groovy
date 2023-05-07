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

class VerifyExceptionSpec extends Specification {
  def 'new'() {
    given:
    def message = 'message'
    def cause = new Exception()

    when:
    def exception = new VerifyException()

    then:
    exception.message == null
    exception.cause == null

    when:
    exception = new VerifyException(message)

    then:
    exception.message == message
    exception.cause == null

    when:
    exception = new VerifyException(message, cause)

    then:
    exception.message == message
    exception.cause === cause

    when:
    exception = new VerifyException(cause)

    then:
    exception.message == cause.toString()
    exception.cause === cause
  }
}
