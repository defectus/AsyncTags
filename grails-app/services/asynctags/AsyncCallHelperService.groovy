package asynctags

import grails.async.Promise
import groovy.transform.CompileStatic
import org.springframework.cache.interceptor.SimpleKeyGenerator
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

import java.lang.reflect.Method

import static grails.async.Promises.task
import static grails.async.Promises.waitAll

@Transactional(readOnly = true)
@CompileStatic
class AsyncCallHelperService {

    static transactional = false
    static final String ASYNC_TAGS_KEY = 'AsyncTags.tags.key'
    static final String ASYNC_ROUND_KEY = 'AsyncTags.round.key'

    String enqueueTask(String key, Closure target) {
        extractPromises() << [(key): task(target)]
        key
    }

    Object dequeueTask(String key, Closure target = {->}) {
        Promise p = extractPromises().get(key)
        !p ? target?.call() : p.get()
    }

    List waitAllTasks() {
        List<Promise> promises = extractPromises().values() as List<Promise>
        waitAll(promises)
    }

    Boolean shouldEnqueue() {
        !!RequestContextHolder.currentRequestAttributes().getAttribute(ASYNC_TAGS_KEY, RequestAttributes.SCOPE_REQUEST)
    }

    void startAsync() {
        RequestContextHolder.currentRequestAttributes().setAttribute(ASYNC_TAGS_KEY, true, RequestAttributes.SCOPE_REQUEST)
    }

    void stopAsync() {
        RequestContextHolder.currentRequestAttributes().setAttribute(ASYNC_TAGS_KEY, false, RequestAttributes.SCOPE_REQUEST)
    }

    RequestAttributes extractRequest() {
        RequestContextHolder.currentRequestAttributes()
    }

    synchronized Map<String, Promise> extractPromises() {
        if (!extractRequest().getAttribute(ASYNC_ROUND_KEY, RequestAttributes.SCOPE_REQUEST)) {
            extractRequest().setAttribute(ASYNC_ROUND_KEY, [:], RequestAttributes.SCOPE_REQUEST)
        }
        return extractRequest().getAttribute(ASYNC_ROUND_KEY, RequestAttributes.SCOPE_REQUEST) as Map<String, Promise>
    }

    String generateKey(Object target, Method method, Object... params) {
        (target.hashCode() as String) + (method.hashCode() as String) +
            ((new SimpleKeyGenerator().generate(target, method, params) as String).hashCode() as String)
    }
}
