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

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class Suppliers {
  public static <T> Supplier<T> memoize(Supplier<T> delegate) {
    return new MemoizedSupplier<>(delegate);
  }

  private Suppliers() {}

  private static final class MemoizedSupplier<T> implements Supplier<T> {
    private final ConcurrentHashMap<Object, Optional<T>> cache = new ConcurrentHashMap<>(1);
    private final Supplier<T> delegate;

    private MemoizedSupplier(Supplier<T> delegate) {
      this.delegate = checkNotNull(delegate);
    }

    @Override
    public T get() {
      return cache.computeIfAbsent("", key -> Optional.ofNullable(delegate.get())).orElse(null);
    }
  }
}
