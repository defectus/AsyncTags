package asynctags

import groovy.util.logging.Log4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.lang.reflect.Method

@Log4j
@Aspect
@Component
class AsyncMethodInterceptor {

    @Autowired
    AsyncCallHelperService asyncCallHelperService

    @Pointcut(value = "execution(public * *(..))")
    public void anyPublicMethod() {
    }

    @Around("anyPublicMethod() && @annotation(asyncMethod)")
    Object invoke(ProceedingJoinPoint pjp, AsyncMethod asyncMethod) throws Throwable {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            Object returnValue = null
            if (asyncCallHelperService.shouldEnqueue()) {
                returnValue = asyncCallHelperService.
                    enqueueTask(asyncCallHelperService.generateKey(pjp.target, method, pjp.args), generateJointPointClosure(pjp))
            } else {
                returnValue = asyncCallHelperService.
                    dequeueTask(asyncCallHelperService.generateKey(pjp.target, method, pjp.args), generateJointPointClosure(pjp))
            }
            return returnValue
        }
        finally {

        }
    }

    Closure generateJointPointClosure(ProceedingJoinPoint pjp) {
        {_ ->
            def returnValue = pjp.proceed()
            returnValue
        }
    }
}
