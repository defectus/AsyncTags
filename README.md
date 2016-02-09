# AsyncTags

[![Build Status](https://travis-ci.org/defectus/AsyncTags.svg?branch=master)](https://travis-ci.org/defectus/AsyncTags)

### A Grails plugin allowing parallel (async) execution of custom tags

This Grails plugin simplifies asynchronization of custom tags. If your tags are actually mini-mvcs (they do more than just rendering provided data) you can certainly use this plugin.

### To install this plugin 

Just modify your `grails-app/conf/BuildConfig.groovy` file. 

### To use this plugin.

First you need to flag your async tags and methods with annotations `@AsyncTag` and `@AsyncMethod` respectively.

In your GSP create an `<async:block>` block. Ideally at the top of the page/layout. Put all your long running tags into it. Once the page gets rendered this will trigger the asynchronous rendering. **Please note that the output of this block will be discarded so do not mix with your other tags!**. This block is to simply trigger the job.

Make sure that your gsp contains the same tags at the places you want the content to be really rendered. As the data becomes available the tags will get the results and can proceed with the normal rendering. 

### Example page

```html
	<html>
		<async:block>
			<l:yourLongRunningTag data="${data1}"/>
			<l:yourOtherLongRunningTag data="${data2}"/>
			...
		</async:block>
		<p>
			<l:yourLongRunningTag data="${data1}"/>
		</p>
		<p>
			<l:yourOtherLongRunningTag data="${data2}"/>
		</p>
	</html> 
```

Your `yourLongRunningTag` may look like this
	
```groovy
	class YourTagLib {
		static namespace = "l"
    	YourService yourService

		@AsyncTag
		def yourLongRunningTag = {attrs, body ->
			yourService.getSomeData(attrs.data)
		}
	}
```

Please note the tag we want to execute asynchronously is flagged with the `@AsyncTag` annotation.

The last step is to make sure the service that provides the data for this tag is flagged with the `@AsyncMethod` annotation.

```groovy
	class YourService {

    	static transactional = false

    	@AsyncMethod
    	def getSomeData(data) {
        	// Thread.sleep(2000)
    	}
	}
```

### Limitations

For this to work you have to stick to several rules.

1. The long running job needs to be in an `@AsyncMethod`. All the `@AsyncTag`s get executed twice so keep that in mind.
2. Having said that you can further improve performance if you use the `AsyncCallHelperService.shouldEnqueue` method to see whether or not to do anything above calling a long running service method.
3. If you call an `@AsyncMethod` twice make sure you provide different parameters or you risk race condition (async key is generated using the parameters).
4. Your `@AsyncMethod`s shouldn't mutate the global state, ideally they should be 'pure'.
5. Any domain classes returned from an `@AsyncMethod` will be `detached`. Make sure you re-attach them before using them.
