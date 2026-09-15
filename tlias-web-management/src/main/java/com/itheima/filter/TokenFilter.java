package com.itheima.filter;

import com.itheima.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
//@WebFilter(urlPatterns = "/*")
@Slf4j
public class TokenFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        //1.获取请求路径
        String requestURI=request.getRequestURI();// 例：/employee/login


        //2.判断是否是登录请求  如果包含/login，说明是登陆操作，要放行
        if(requestURI.contains("/login")){
            log.info("登录请求，放行");
            chain.doFilter(request, response);
            return;
        }


        //3.获取请求头的token
        String token = request.getHeader("token");


        //4.判断token是否合法存在，如果不存在，说明没有登陆，返回错误信息（响应401状态码）
        if(token == null || token.isEmpty()){
            log.info("令牌为空，响应401");
            response.setStatus(401);
            return;
        }


        //5.如果token存在，校验令牌，如果校验失败，则返回错误信息（响应401状态码）
        try {
            JwtUtils.parseJwt(token);
        } catch (Exception e) {
            log.info("令牌非法，响应401");
            response.setStatus(401);
            return;
        }


        //6.通过则放行
        log.info("令牌合法，放行");
        chain.doFilter(request, response);


    }
}