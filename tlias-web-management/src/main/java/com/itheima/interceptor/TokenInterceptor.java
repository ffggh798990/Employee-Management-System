package com.itheima.interceptor;

import com.itheima.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {



            //1.获取请求路径
            String requestURI=request.getRequestURI();// 例：/employee/login


            //2.判断是否是登录请求  如果包含/login，说明是登陆操作，要放行
            if(requestURI.contains("/login")){
                log.info("登录请求，放行");

                return true;
            }


            //3.获取请求头的token
            String token = request.getHeader("token");


            //4.判断token是否合法存在，如果不存在，说明没有登陆，返回错误信息（响应401状态码）
            if(token == null || token.isEmpty()){
                log.info("令牌为空，响应401");
                response.setStatus(401);
                return false;
            }


            //5.如果token存在，校验令牌，如果校验失败，则返回错误信息（响应401状态码）
            try {
                JwtUtils.parseJwt(token);
            } catch (Exception e) {
                log.info("令牌非法，响应401");
                response.setStatus(401);
                return false;
            }


            //6.通过则放行
            log.info("令牌合法，放行");
            return true;


    }
}
