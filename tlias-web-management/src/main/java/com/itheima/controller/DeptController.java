package com.itheima.controller;

import com.itheima.anno.Log;
import com.itheima.pojo.Dept;
import com.itheima.pojo.Result;
import com.itheima.service.DeptService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
//@RequestMapping("/depts")可以合并同类项，下面的就可以不写depts了
@RestController
public class DeptController {

    //private static final Logger log= LoggerFactory.getLogger(DeptController.class);//固定的。字节码对象才改
    @Autowired
    private DeptService deptService;

    //@RequestMapping(value = "/depts",method = RequestMethod.GET)
    @GetMapping("/depts")
    public Result list()
    {
        //System.out.println("查询全部部门信息");
        log.info("查询全部部门数据");
        List<Dept> deptlist =deptService.findAll();
        return Result.success(deptlist);
    }

//    @DeleteMapping("/depts")
//    public Result delete(@RequestParam(value = "id",required = false) Integer deptId)//request=true时，不传递会报错，必须传递参数
//    {
//        System.out.println("根据id删除部门： "+ deptId);
//
//        return Result.success();
//    }


    @Log//标记该接口需要记录操作日志
    @DeleteMapping("/depts")//("/{id}")加上前面request mapping的请求路径共同成为总的请求路径
    //注意：前端 api/dept.js 里发的是 request.delete(`/depts?id=${id}`)，是"问号传参"DELETE /depts?id=3，
    //不是"路径传参"DELETE /depts/3。所以这里必须是 /depts + @RequestParam 才能匹配上，
    //写成 /depts/{id} + @PathVariable 会报 405 Request method 'DELETE' is not supported。
    public Result delete(@RequestParam Integer id) {
        //System.out.println("根据id删除部门： "+ id);//只能输出到控制台不便于维护以及拓展
       log.info("根据id删除部门： {}",id);
        deptService.deleteById(id);

        return Result.success();
    }

    @Log//标记该接口需要记录操作日志
    @PostMapping("/depts")
    public Result add(@RequestBody Dept dept){
        //System.out.println("新增部门： "+ dept);
        log.info("新增部门： {}",dept);
        deptService.add(dept);
        return Result.success();
    }
    @GetMapping("/depts/{id}")
    public Result getInfo(@PathVariable Integer id)
    {
        //System.out.println("根据ID查询部门： "+id);
        log.info("根据id查询部门： {}",id);
        Dept dept=deptService. getByid(id);
        return Result.success(dept);
    }
    @Log//标记该接口需要记录操作日志
    @PutMapping("/depts")
    public  Result updateById(@RequestBody Dept dept)
    {
        //System.out.println("修改部门： "+dept);
        log.info("修改部门： {}",dept);
        deptService.updateById(dept);
        return Result.success();
    }


}
