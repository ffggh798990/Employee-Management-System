package com.itheima.utils;

/**
 * 用ThreadLocal在一次请求的线程内共享"当前登录员工ID"。
 * TokenInterceptor解析完JWT后存进来，LogAspect记录操作人时取出来。
 * Tomcat线程是复用的，所以请求结束必须remove，否则会串数据。
 */
public class BaseContext {

    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Integer id) {
        THREAD_LOCAL.set(id);
    }

    public static Integer getCurrentId() {
        return THREAD_LOCAL.get();
    }

    public static void removeCurrentId() {
        THREAD_LOCAL.remove();
    }
}
