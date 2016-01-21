package asynctags

class AsyncTagLib {

    static namespace = "async"
    AsyncCallHelperService asyncCallHelperService

    /**
     * Creates an async block. All calls to service methods annotated with {@link AsyncMethod} will be executed asynchronously
     *
     * @attr
     */
    Closure block = {Map attrs, Closure body ->
        asyncCallHelperService.startAsync()
        body()
        asyncCallHelperService.stopAsync()
        asyncCallHelperService.waitAllTasks()
        out << body()
    }
}
