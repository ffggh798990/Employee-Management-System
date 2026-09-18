package com.itheima.interceptor;

import com.itheima.utils.BaseContext;
import com.itheima.utils.JwtUtils;
import io.jsonwebtoken.Claims;
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

                //令牌校验通过，再解析一次取出自定义信息里的员工ID，放进本次请求的线程上下文。
                //后面 LogAspect 记录"操作人"时要从这里取（切面拿不到 HttpServletRequest）
                Claims claims = JwtUtils.parseJwt(token);
                Object id = claims.get("id");
                if (id != null) {
                    //JWT 解出来的数字可能是 Integer / Long，统一转成 Integer
                    BaseContext.setCurrentId(((Number) id).intValue());
                }
            } catch (Exception e) {
                log.info("令牌非法，响应401");
                BaseContext.removeCurrentId();
                response.setStatus(401);
                return false;
            }


            //6.通过则放行
            log.info("令牌合法，放行");
            return true;


    }

    /**
     * 请求处理完成后清理 ThreadLocal。
     * Tomcat 的线程是复用的（线程池），不清理的话下一个请求可能读到上一个请求残留的员工ID。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
