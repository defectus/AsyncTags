package asynctags

import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.DefaultGrailsTagLibClass
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.pages.FastStringWriter
import org.codehaus.groovy.grails.web.pages.GroovyPageOutputStack
import org.codehaus.groovy.grails.web.taglib.GroovyPageAttributes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@Log4j
class PostProcessor {

    @Autowired
    GrailsApplication grailsApplication
    @Autowired
    AsyncCallHelperService asyncCallHelperService

    ApplicationContext postProcess(ApplicationContext ctx) {
        for (DefaultGrailsTagLibClass clazz : grailsApplication.tagLibClasses) {
            for (String tagName : clazz.clazz.declaredFields.findAll {it.getAnnotation(AsyncTag)}*.name) {
                String tagGetterName = MetaProperty.getGetterName(tagName, clazz.clazz)
                MetaMethod tagGetter = clazz.clazz.metaClass.getMetaMethod(tagGetterName, null)
                clazz.clazz.metaClass."$tagGetterName" = createTagWrappingClosure tagGetter
            }
        }
        ctx
    }

    private Object callOriginalTag(Closure originalClosure, def attrs, def body) {
        def returnValue
        if (originalClosure.parameterTypes.size() == 0) {
            returnValue = originalClosure.call()
        } else if (originalClosure.parameterTypes.size() == 1) {
            returnValue = originalClosure.call(attrs)
        } else {
            returnValue = originalClosure.call(attrs, body)
        }
        return returnValue
    }

    Closure createTagWrappingClosure(MetaMethod tagGetter) {
        {->
            Closure originalTag = tagGetter.invoke(delegate) as Closure
            return {GroovyPageAttributes attrs, body ->
                Writer capturedOutWriter = new FastStringWriter()
                GroovyPageOutputStack.currentStack(originalTag.webRequest).push(capturedOutWriter);
                Object returnValue
                try {
                    returnValue = callOriginalTag(originalTag, attrs, body)
                    if (asyncCallHelperService.shouldEnqueue()) {
                        capturedOutWriter = new FastStringWriter()
                        returnValue = ""
                    }
                }
                catch (Exception e) {
                    log.error("An exception occured when executing tag <${tagGetter.name} ${attrs} />", e)
                }
                finally {
                    GroovyPageOutputStack.currentStack(originalTag.webRequest).pop();
                }
                originalTag.out << capturedOutWriter
                return returnValue
            }
        }
    }
}
