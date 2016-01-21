package asynctags

import org.aopalliance.aop.Advice
import org.springframework.aop.Pointcut
import org.springframework.aop.support.AbstractPointcutAdvisor
import org.springframework.aop.support.StaticMethodMatcherPointcut
import org.springframework.beans.factory.annotation.Autowired

import java.lang.reflect.Method

class AsyncMethodAdvisor extends AbstractPointcutAdvisor {

    private final StaticMethodMatcherPointcut pointcut = new
        StaticMethodMatcherPointcut() {

            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return method.isAnnotationPresent(AsyncMethod)
            }
        }

    @Autowired
    private AsyncMethodInterceptor interceptor

    @Override
    public Pointcut getPointcut() {
        return this.pointcut
    }

    @Override
    public Advice getAdvice() {
        return this.interceptor
    }
}
