package com.zzg.springcloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.zzg.springcloud.entities.Dept;
// 当你没有配置扫描的包的时候，就要是要这个注解
@Mapper
public interface DeptDao
{
	public boolean addDept(Dept dept);

	public Dept findById(Long id);

	public List<Dept> findAll();
}
