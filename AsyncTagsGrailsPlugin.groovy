import asynctags.PostProcessor
import asynctags.AsyncCallHelperService
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
