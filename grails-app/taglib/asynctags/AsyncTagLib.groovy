package asynctags

class AsyncTagLib {

    static namespace = "async"
    AsyncCallHelperService asyncCallHelperService

    /**
     * Creates an async block. All calls to service methods annotated with {@link AsyncMethod} will be executed asynchronously
     *
     * @attr waitForAll wait for all tasks to complete
     */
    Closure block = {Map attrs, Closure body ->
        asyncCallHelperService.startAsync()
        body()
        asyncCallHelperService.stopAsync()
        'true'.equalsIgnoreCase(attrs.waitForAll) ? asyncCallHelperService.waitAllTasks() : null
    }
}
