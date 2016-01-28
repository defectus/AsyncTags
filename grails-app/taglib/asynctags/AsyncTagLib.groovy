/*
 * Copyright 2015 Ondřej Linek
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

package asynctags

class AsyncTagLib {

    static namespace = "async"

    AsyncCallHelperService asyncCallHelperService

    /**
     * Creates an async block. All calls to service methods annotated with {@link AsyncMethod} will be executed asynchronously
     *
     * @attr waitForAll wait for all tasks to complete
     */
    def block = { Map attrs, Closure body ->
        asyncCallHelperService.startAsync()
        body()
        asyncCallHelperService.stopAsync()
        'true'.equalsIgnoreCase(attrs.waitForAll) ? asyncCallHelperService.waitAllTasks() : null
    }
}
