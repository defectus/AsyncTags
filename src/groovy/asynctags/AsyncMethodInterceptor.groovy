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
import groovy.util.logging.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.lang.reflect.Method

@Slf4j
@Aspect
@Component
@CompileStatic
class AsyncMethodInterceptor {

    @Autowired
    AsyncCallHelperService asyncCallHelperService

    @Pointcut(value = "execution(public * *(..))")
    void anyPublicMethod() {
    }

    @Around("anyPublicMethod() && @annotation(asyncMethod)")
    def invoke(ProceedingJoinPoint pjp, AsyncMethod asyncMethod) {
        def returnValue
        try {
            MethodSignature signature = (MethodSignature) pjp.signature
            Method method = signature.method
            if (asyncCallHelperService.shouldEnqueue()) {
                returnValue = asyncCallHelperService.
                    enqueueTask(asyncCallHelperService.generateKey(pjp.target, method, pjp.args), generateJointPointClosure(pjp))
            } else {
                returnValue = asyncCallHelperService.
                    dequeueTask(asyncCallHelperService.generateKey(pjp.target, method, pjp.args), generateJointPointClosure(pjp))
            }
        }
        finally {
            return returnValue
        }
    }

    Closure generateJointPointClosure(ProceedingJoinPoint pjp) {
        {_ ->
            pjp.proceed()
        }
    }
}
