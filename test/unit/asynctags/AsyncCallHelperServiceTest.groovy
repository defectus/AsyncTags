package asynctags

import grails.async.Promise
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
@ContextConfiguration(locations = "classpath:/spring/async.xml")
@TestFor(AsyncCallHelperService)
class AsyncCallHelperServiceTest extends Specification {

    void "enqueueTask stores a task into the request"() {
        given:
            MockHttpServletRequest request = new MockHttpServletRequest()
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
        when:
            String returnValue = service.enqueueTask('12345', {-> '12345'})
        then:
            returnValue
            RequestContextHolder.getRequestAttributes().
                getAttribute(AsyncCallHelperService.ASYNC_ROUND_KEY, RequestAttributes.SCOPE_REQUEST)['12345'] instanceof Promise
    }

    void "dequeueTask stores a task into the request"() {
        given:
            MockHttpServletRequest request = new MockHttpServletRequest()
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
            service.enqueueTask('12345', {-> '12345'})
        when:
            String returnValue = service.dequeueTask('12345')
        then:
            returnValue == '12345'
    }

    void "can generate unique key"() {
        given:
            FakeService fakeService = new FakeService()
        when:
            String key1 = service.generateKey(fakeService, FakeService.getMethod('fakeMethod', String, String), ['a', 'b'])
            String key2 = service.generateKey(fakeService, FakeService.getMethod('fakeMethod', String, String), ['c', 'd'])
        then:
            key1
            key2
            key1 != key2
    }

    void "can wait for all tasks"() {
        given:
            MockHttpServletRequest request = new MockHttpServletRequest()
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
        when:
            service.enqueueTask('12345', {-> '12345'})
            service.enqueueTask('6789A', {-> '6789A'})
        then:
            service.waitAllTasks() == ['12345', '6789A']
    }

    void "async block flags work"() {
        given:
            MockHttpServletRequest request = new MockHttpServletRequest()
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
        when:
            service.startAsync()
        then:
            service.shouldEnqueue()
        when:
            service.stopAsync()
        then:
            !service.shouldEnqueue()
    }
}
