package com.itheima.service;

import com.itheima.pojo.Dept;

import java.util.List;

public interface DeptService {

    List<Dept> findAll();

    void deleteById(Integer id);

    void add(Dept dept);

    Dept getByid(Integer id);

    void updateById(Dept dept);
}
