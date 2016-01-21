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

import asynctags.AsyncCallHelperService
import asynctags.PostProcessor
import org.springframework.context.ApplicationContext

class AsyncTagsGrailsPlugin {

    def version = "1.0.0"
    def grailsVersion = "2.2 > *"
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Async Tags Plugin"
    def author = "Ondrej Linek"
    def authorEmail = "ondrej.linek@gmail.com"
    def description = 'A small plugin that makes (ideally by convention) selected tags'

    def documentation = "http://grails.org/plugin/async-tags"

    def license = "APACHE"

    def doWithWebDescriptor = {xml ->
    }

    def doWithSpring = {

        asyncCallHelperService(AsyncCallHelperService)

        postProcessor(PostProcessor) {
            grailsApplication = application
            asyncCallHelperService = asyncCallHelperService
        }
        importBeans('classpath:/spring/async.xml')
    }

    def doWithDynamicMethods = {ctx ->
    }

    def doWithApplicationContext = {ApplicationContext ctx ->
        ctx.getBean(PostProcessor).postProcess ctx
    }

    def onChange = {event ->
    }

    def onConfigChange = {event ->
    }

    def onShutdown = {event ->
    }
}
