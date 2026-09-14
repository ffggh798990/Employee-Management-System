package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.EmpExprMapper;
import com.itheima.mapper.EmpLogMapper;
import com.itheima.mapper.EmpMapper;
import com.itheima.pojo.*;
import com.itheima.service.EmpLogService;
import com.itheima.service.EmpService;
import com.itheima.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class EmpServiceImpl implements EmpService {

    @Autowired//注入mapper接口
    private EmpMapper empMapper;
    @Autowired
    private EmpExprMapper empExprMapper;
    @Autowired
    private EmpLogMapper empLogMapper;
    @Autowired
    private EmpLogService empLogService;

    //    @Override    原始分页查询
//    public PageResult<Emp> page(Integer page, Integer pageSize) {
//        Long total=empMapper.count();
//        Integer start=(page-1)*pageSize;
//        List<Emp> rows=empMapper.list(start,pageSize);
//
//        return new PageResult<Emp>(total,rows);
//    }
    //使用page helper后的查询
//    @Override
//    public PageResult<Emp> page(Integer page, Integer pageSize,String name, Integer gender, LocalDate begin, LocalDate end) {
//        //设置分页参数page helper
//        PageHelper.startPage(page,pageSize);
//        //执行查询
//        List<Emp> emplist = empMapper.list(name,gender, begin, end);
//        //解析结果并封装数据
//        Page<Emp> p=(Page<Emp>) emplist;
//
//
//        return new PageResult<Emp>(p.getTotal(),p.getResult());
//    }
@Override
public PageResult<Emp> page(EmpQueryParam empQueryParam) {
    //设置分页参数page helper
    PageHelper.startPage(empQueryParam.getPage(),empQueryParam.getPageSize());
    //执行查询
    List<Emp> emplist = empMapper.list(empQueryParam);
    //解析结果并封装数据
    Page<Emp> p=(Page<Emp>) emplist;


    return new PageResult<Emp>(p.getTotal(),p.getResult());
}


@Transactional(rollbackFor = {Exception.class})//任何异常都会回滚
//业务层的方法上，类上，接口上  交给spring进行事务管理
    @Override
    public void save(Emp emp) {
        //1.保存员工基本信息
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.insert(emp);

        //2.保存员工工作经历
        List<EmpExpr> exprList=emp.getExprList();
         if(!CollectionUtils.isEmpty(exprList))
         {
             //遍历集合，为EmpId赋值

             exprList.forEach(empExpr -> {
                 empExpr.setEmpId(emp.getId());
                     }
             );
             empExprMapper.insertBatch(exprList);
         }

         //记录操作日志
    //
    EmpLog empLog=new EmpLog(null,LocalDateTime.now(),"新增员工："+emp);
    empLogService.insertLog(empLog);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void delete(List<Integer> ids) {
        //删除员工基本信息
        empMapper.deleteById(ids);



        //删除员工工作经历日期

        empExprMapper.deleteByEmpIds(ids);
    }

    //

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void update(Emp emp) {

        //1.根据id修改员工基本信息
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.updateById(emp);

        //2.根据id修改员工工作经历信息
        //2.1 先根据id删除原有的所有工作经历信息
        empExprMapper.deleteByEmpIds(Arrays.asList(emp.getId()));//将单个id包装成一个list集合

        //2.2再添加新的工作经历信息
        List<EmpExpr> exprList=emp.getExprList();
        if(!CollectionUtils.isEmpty(exprList))
        {
            exprList.forEach(empExpr -> empExpr.setEmpId(emp.getId()));
            //格式 ：(参数) -> { 具体要执行的代码 }
            empExprMapper.insertBatch(exprList);
        }
    }


    @Override
    public LoginInfo login(Emp emp) {
    //1.调用mapper接口，根据用户名和密码查询员工信息
     Emp e=   empMapper.selectByUsernameAndPassword(emp);

    //2.判断：是否存在这个员工，如果存在，组装登陆返回信息；
     if(e !=null)
     {
         log.info("登陆成功，员工信息：{}",e);
         //生成JWT令牌
         Map<String,Object> claims= new HashMap<>();
         claims.put("id",e.getId());
         claims.put("username",e.getUsername());
         String jwt = JwtUtils.generateJwt(claims);
         return new LoginInfo(e.getId(),e.getUsername(),e.getName(),jwt);
     }

    //3.否则，返回null。
        return null;
    }



    @Override
    public Emp getInfo(Integer id) {
        return empMapper.getById(id);
    }
}
