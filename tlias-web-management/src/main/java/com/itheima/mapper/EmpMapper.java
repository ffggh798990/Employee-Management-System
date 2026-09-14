package com.itheima.mapper;

import com.itheima.pojo.Emp;
import com.itheima.pojo.EmpQueryParam;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface EmpMapper {
//    @Select("select  count(*) from emp")
//    public Long count();

//    @Select("select e.*, d.name deptName from emp e left join dept d on e.dept_id = d.id " +
//            "order by e.update_time desc limit #{start},#{pageSize};")
//   public List<Emp> list(Integer start,Integer pageSize);

        //@Select("select e.*, d.name deptName from emp e left join dept d on e.dept_id = d.id " +
                //"order by e.update_time desc ")
//   public List<Emp> list(String name, Integer gender, LocalDate begin, LocalDate end);

 public List<Emp> list(EmpQueryParam empQueryParam);

@Options(useGeneratedKeys = true,keyProperty = "id")//获取到生成的主键 --主键返回
//因为id是自增的，默认你不会拿到这个id值，但是可以通过mybatis返回这个结果

@Insert("insert into emp(username,  name, gender, phone, job, salary, image, entry_date, dept_id, create_time, update_time)" +
        "  values(#{username},#{name},#{gender},#{phone},#{job},#{salary},#{image},#{entryDate},#{deptId},#{createTime},#{updateTime})")
        void insert(Emp emp);//调用完会将id拿到并复制到emp当中。。


    void deleteById(List<Integer> ids);


    Emp getById(Integer id);

    /*根据id更新员工的基本信息*/
    void updateById(Emp emp);

    List<Map<String,Object>> countEmpJobData();

    List<Map<String, Object>> countEmpGenderData();

    Emp selectByUsernameAndPassword(Emp emp);
}
