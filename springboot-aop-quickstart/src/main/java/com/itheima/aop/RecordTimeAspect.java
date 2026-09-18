package com.itheima.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
//@Aspect//标识当前是一个Aop类 切面类
@Component
public class RecordTimeAspect {

    /*切入点表达式
    execution  主要根据方法的返回值、包名、类名、方法名、方法参数等信息来匹配
    语法：
    execution（访问修饰符？  返回值  包名.类名.？ 方法名（方法参数）  throws  异常？）
    带？的都是可以省略的
    访问修饰符：比如public protected
    包名类名强烈不建议省略
    throws是方法上声明抛出的异常，不是实际抛出的异常

    * ：单个独立的任意符号，可以通配任意返回值，包名，类名，方法名，任意类型的一个参数或者包类方法名的一部分
    execution(* com.*.service.*.update*(*))
    ..：多个连续的任意符号，可以通配任意层级的包，或者任意类型、任意个数的参数
    execution(* com.itheima..DeptService.*(..))
    */

    @Around("execution(* com.itheima.service.impl.*.*(..))")//匹配连接点的条件，切入点表达式
    public Object recordTime(ProceedingJoinPoint pjp) throws Throwable {
        //通知     通知+切入点=切面。
        //1.记录方法运行的开始时间
        long begin=System.currentTimeMillis();
        //2.执行原始的方法  环绕通知需要自己调用.proceed()来让原始方法执行，其它通知不需要考虑目标方法的执行
        Object result=pjp.proceed();
        //返回值类型必须是Object来接受原始方法的返回值

        //3.记录方法运行的结束时间，计算执行耗时
        long end=System.currentTimeMillis();
        log.info("方法 {} 执行耗时：{}ms",pjp.getSignature(),end-begin);
        return result;
    }
}
//1.@Around  环绕通知
//2.@Before  前置通知
//3.@After    后置通知
//4.@AfterReturning  返回后通知 有异常不执行
//5.@AfterThrowing  异常后通知，发生异常后执行