package com.kskj.until;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ThreadMonitorAspect {
    @Autowired
    private LuaThreadMonitor luaMonitor;

    // 监控所有 Service 层方法
    @Around("execution(* com.kskj.service..*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethod(joinPoint, "SERVICE");
    }
    // 监控所有 Controller 层方法
    @Around("execution(* com.kskj.controller..*(..))")
    public Object monitorControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethod(joinPoint, "CONTROLLER");
    }
    //线程监控方法配合lua使用，可用于检查线程问题。但是使用之前需要检查一下依赖，看看注释没有
    private Object monitorMethod(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long threadId = Thread.currentThread().getId();

        // 记录方法开始
        luaMonitor.logMethod(layer + ":" + methodName, threadId, "START");

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();

            // 记录方法成功结束
            long duration = System.currentTimeMillis() - startTime;
            luaMonitor.logMethod(layer + ":" + methodName, threadId,
                    "SUCCESS(" + duration + "ms)");

            return result;

        } catch (Exception e) {
            // 记录方法异常结束
            long duration = System.currentTimeMillis() - startTime;
            luaMonitor.logMethod(layer + ":" + methodName, threadId,
                    "ERROR(" + duration + "ms): " + e.getClass().getSimpleName());
            throw e;
        }
    }
    /*
    下面的方法为使用AOP来自动加日志。
     */
}
