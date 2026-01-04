package com.epam.finaltask.AOP.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class MethodLoggingAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {}

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceMethods() {}

    @Pointcut("execution(* com.epam.finaltask.service.AbstractTokenStorage.*(..))")
    public void tokenStorageMethod() {}

    @Pointcut("execution(* com.epam.finaltask.util.JwtUtil.*(..)) || execution(* com.epam.finaltask.util.ResetTokenUtil.*(..))")
    public void jwtTokenMethod() {}

    @Pointcut("restControllerMethods() || serviceMethods() || tokenStorageMethod() || jwtTokenMethod()")
    public void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()" )
    public Object logExecutionMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();

        log.info("stage=init-{}, method={}, controller={}, parameters={}",
                joinPoint.getSignature().getName(),
                joinPoint.getSignature().getName(),
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getArgs());

        Object result = joinPoint.proceed();
        long timeElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        log.info("stage=finish-{}, method={}, controller={}, parameters={}, result={}, time-execution={}ms",
                joinPoint.getSignature().getName(),
                joinPoint.getSignature().getName(),
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getArgs(),
                result,
                timeElapsed);

        return result;
    }

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.error("Exception in {}.{}() with cause = {}",
                className, methodName, e.getCause() != null ? e.getCause() : "NULL", e);
    }
}
