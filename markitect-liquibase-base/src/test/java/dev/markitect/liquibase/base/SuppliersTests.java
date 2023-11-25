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

package dev.markitect.liquibase.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SuppliersTests {
  @Mock private Supplier<String> delegate;

  @Test
  void memoize() {
    // given
    given(delegate.get()).willReturn("value");

    // when
    var supplier = Suppliers.memoize(delegate);

    // then
    assertThat(supplier).isNotNull();

    // when
    String value1 = supplier.get();
    String value2 = supplier.get();

    // then
    assertThat(value1).isEqualTo("value");
    assertThat(value2).isEqualTo("value");
    verify(delegate).get();
  }
}
