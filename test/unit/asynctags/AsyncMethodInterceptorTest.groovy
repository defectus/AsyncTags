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
