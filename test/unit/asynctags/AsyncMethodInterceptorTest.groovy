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

import grails.test.mixin.TestFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification

@ContextConfiguration(locations = ["classpath:/spring/async.xml","classpath:/spring/async-test.xml"])
@TestFor(AsyncCallHelperService)
class AsyncMethodInterceptorTest extends Specification {

    @Autowired
    FakeService fakeService

    void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
    }

    void "method intercepted when in async mode"() {
        given:
            service.startAsync()
        when:
            String result = fakeService.fakeMethod('a', 'b')
        then:
            result != 'ab'
    }

    void "method executed when not in async mode"() {
        given:
            service.stopAsync()
        when:
            String result = fakeService.fakeMethod('a', 'b')
        then:
            result == 'ab'
    }

    void "method result returned after async mode"() {
        given:
            service.startAsync()
            fakeService.fakeMethod('a', 'b')
            service.stopAsync()
        when:
            String result = fakeService.fakeMethod('a', 'b')
        then:
            result == 'ab'
    }
}
