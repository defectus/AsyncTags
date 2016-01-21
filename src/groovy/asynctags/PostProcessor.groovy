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

import groovy.transform.CompileStatic
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

    @CompileStatic
    ApplicationContext postProcess(ApplicationContext ctx) {
        for (DefaultGrailsTagLibClass clazz : extractAllTagLibs()) {
            for (String tagName : clazz.clazz.declaredFields.findAll {it.getAnnotation(AsyncTag)}*.name) {
                String tagGetterName = MetaProperty.getGetterName(tagName, clazz.clazz)
                MetaMethod tagGetter = clazz.clazz.metaClass.getMetaMethod(tagGetterName)
                replaceGetter(tagGetter, clazz, tagGetterName)
            }
        }
        ctx
    }

    List<DefaultGrailsTagLibClass> extractAllTagLibs() {
        return grailsApplication.tagLibClasses*.asType(DefaultGrailsTagLibClass)
    }

    void replaceGetter(MetaMethod tagGetter, DefaultGrailsTagLibClass clazz, String tagGetterName) {
        clazz.clazz.metaClass."$tagGetterName" = createTagWrappingClosure tagGetter
    }

    @CompileStatic
    Object callOriginalTag(Closure originalClosure, def attrs, def body) {
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
