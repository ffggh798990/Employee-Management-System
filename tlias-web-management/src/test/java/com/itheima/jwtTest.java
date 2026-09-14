package com.itheima;

import com.jayway.jsonpath.Configuration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class jwtTest {
    @Test
    public  void testGenerateJwt()
    {
        Map<String, Object> dataMap=new HashMap<>();
        dataMap.put("id",1);
        dataMap.put("username","admin");

        String jwt= Jwts.builder().signWith(SignatureAlgorithm.HS256,"aXRoZWltYQ==")//指定加密算法以及密钥
                .addClaims(dataMap)//添加自定义信息
                .setExpiration(new Date(System.currentTimeMillis()+3600*2000))//设置时间期效
                .compact();
        System.out.println(jwt);


    }
    @Test
    public void testParseJWT()
    {
        String token="eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJhZG1pbiIsImV4cCI6MTc4Mzg2OTU3MH0.EAXe5IBCtJ31MIiFNyD2Rs_3tTTLYZzgiLCwp8NWSlw";
        Claims claims=Jwts.parser().setSigningKey("aXRoZWltYQ==")//指定密钥
                .parseClaimsJws(token)//解析令牌
                .getBody();//获取自定义信息
        System.out.println(claims);
    }
}
