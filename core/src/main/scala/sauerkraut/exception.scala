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


/** An exception during pickling/unpickling. */
class SauerkrautException(msg: String, cause: Throwable)
  extends RuntimeException(msg, cause)

class BuildException(msg: String, cause: Throwable)
  extends SauerkrautException(msg, cause)

class WriteException(msg: String, cause: Throwable)
  extends SauerkrautException(msg, cause)