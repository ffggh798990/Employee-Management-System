package com.itheima.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@Order(3)
public class MyAspect4 {
    //前置通知
    //@Before("execution(public void com.itheima.service.impl.DeptServiceImpl.delete(java.lang.Integer))")
    //@Before("execution( void com.itheima.service.impl.DeptServiceImpl.delete(java.lang.Integer))")
    //@Before("execution(public void delete(java.lang.Integer))")  不建议省略，扫描范围变大性能降低

    //@Before("execution(* com.itheima.service.impl.DeptServiceImpl.delete(java.lang.Integer))")
    //@Before("execution(* com.*.service.impl.DeptServiceImpl.delete(java.lang.Integer))")
//    @Before("execution(* com.itheima.service.impl.*.delete(java.lang.Integer))")
    //@Before("execution(* com.itheima.service.impl.*.*(java.lang.Integer))")//不要求方法名，匹配到了两个
    //@Before("execution(* com.itheima.service.impl.*.*(*))")//*限定只有一个参数
    //@Before("execution(* com.itheima.service.impl.*.del*(*))")//限定del开头的方法名
    //@Before("execution(* com.itheima.service.impl.*.*e(*))")
    //@Before("execution(*  com..service.impl.DeptServiceImpl.delete(..))")//任意包下的service。。。    任意个任意类型的参数
    //匹配list方法和delete方法
//    @Before("execution(* com.itheima.service.impl.DeptServiceImpl.list(..))||" +
//            "execution(* com.itheima.service.impl.DeptServiceImpl.delete(..))")
//
    @Before("@annotation(com.itheima.anno.LogOperation)")

    public void before(){
        log.info("MyAspect4 -> before ...");
    }

    //后置通知
    @After("execution(* com.itheima.service.impl.*.*(..))")
    public void after(){
        log.info("MyAspect4 -> after ...");
    }
}
