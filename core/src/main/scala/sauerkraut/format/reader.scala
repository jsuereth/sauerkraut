/*
 * Copyright 2019 Google
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

package sauerkraut
package format

import scala.collection.mutable.Builder
/**
 * A reader of pickles.  This should be able to handle the possible
 * [[core.Builder]] types.
 */
trait PickleReader
  /** Pushes the contents of the pickle into the builder. */
  def push[T](builder: core.Builder[T]): core.Builder[T]
