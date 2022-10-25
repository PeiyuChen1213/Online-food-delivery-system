package com.chenpeiyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpeiyu.entity.Employee;
import com.chenpeiyu.mapper.EmployeeMapper;
import com.chenpeiyu.service.EmployeeService;
import org.springframework.stereotype.Service;

//创建service层的实现类 是根据mybatis——plus自动生成的
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}
