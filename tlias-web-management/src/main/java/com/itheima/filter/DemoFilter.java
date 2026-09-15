package com.itheima.filter;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

//@WebFilter(urlPatterns = "/*")//拦截所有请求

@Slf4j
public class DemoFilter implements Filter {
    @Override
    //初始化方法，web服务器启动的时候执行，只执行一次
    public void init(FilterConfig filterConfig) throws ServletException {

        log.info("init 初始化方法......");
    }

    //拦截到请求之后执行，会执行多次
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        log.info("拦截到了请求......");
        //放行

        chain.doFilter(request,response);

    }

    @Override
    public void destroy() {
        log.info("destroy 销毁方法......");
        //关闭时运行，执行一次，资源销毁管理工作
    }
}
