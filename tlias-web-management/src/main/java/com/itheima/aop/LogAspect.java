package com.itheima.aop;

import com.itheima.mapper.OperateLogMapper;
import com.itheima.pojo.OperateLog;
import com.itheima.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect//标识当前是一个切面类
@Component
public class LogAspect {

    @Autowired
    private OperateLogMapper operateLogMapper;

    //用Spring容器里配好的Jackson。不要自己new，它自带java时间类型的支持
    @Autowired
    private ObjectMapper objectMapper;

    //环绕通知：只有它同时拿得到 方法参数、返回值、执行耗时
    //切入点写全类名，避免和lombok生成的log字段混在一起
    @Around("@annotation(com.itheima.anno.Log)")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {
        //1.操作时间
        LocalDateTime operateTime = LocalDateTime.now();
        //2.操作人ID，由TokenInterceptor放进了ThreadLocal
        Integer operateEmpId = BaseContext.getCurrentId();
        //3.目标类全类名。getTarget()拿原始对象，避免拿到代理类的名字
        String className = joinPoint.getTarget().getClass().getName();
        //4.方法名
        String methodName = joinPoint.getSignature().getName();
        //5.方法参数
        String methodParams = Arrays.toString(joinPoint.getArgs());

        long begin = System.currentTimeMillis();
        Object result = joinPoint.proceed();//执行原始方法
        long end = System.currentTimeMillis();

        //6.返回值
        String returnValue = objectMapper.writeValueAsString(result);
        //7.执行耗时
        long costTime = end - begin;

        //封装成OperateLog对象并保存
        OperateLog operateLog = new OperateLog();

        operateLog.setOperateEmpId(operateEmpId);
        operateLog.setOperateTime(operateTime);
        operateLog.setClassName(className);
        operateLog.setMethodName(methodName);
        operateLog.setMethodParams(methodParams);
        operateLog.setReturnValue(returnValue);
        operateLog.setCostTime(costTime);
        operateLogMapper.insert(operateLog);

        log.info("AOP记录操作日志：{}", operateLog);
        return result;//一定要把原始返回值返回，否则接口拿不到数据
    }
}
