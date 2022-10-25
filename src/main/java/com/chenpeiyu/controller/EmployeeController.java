package com.chenpeiyu.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpeiyu.common.R;
import com.chenpeiyu.entity.Employee;
import com.chenpeiyu.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1 将页面提交的密码password 进行 md5 加密
        String password = employee.getPassword();
        //将封装的密码进行加密然后再赋值给原来的变量名
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2 根据页面提交的用户名username查询数据库中员工数据信息
        //创建一个查询包装类
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());

        //根据service 来查询单个
        Employee emp = employeeService.getOne(lambdaQueryWrapper);
        //3. 如果没有查询到, 则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败,用户名不存在！");
        }
        // 4.进行密码的比对，如果不一致则返回登陆失败
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败,密码错误!");
        }

        //5.查看员工状态，如果员工的状态是已经禁用状态，则返回员工已经禁用结果

        if (emp.getStatus() == 0) {
            return R.error("账号已经禁用!");
        }

        //6.登录成功，将员工的id存入Session 并返回登录成功的结果

        request.getSession().setAttribute("employee", emp.getId());
        System.out.println(R.success(emp));
        return R.success(emp);
    }

    /**
     * A. 清理Session中的用户id
     * B. 返回结果
     *
     * @param request 移除session中的属性
     * @return
     */

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理session中的用户id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }

    //在EmployeeController中增加save方法, 用于保存用户员工信息。
    //
    //A. 在新增员工时， 按钮页面原型中的需求描述， 需要给员工设置初始默认密码 123456， 并对密码进行MD5加密。
    //
    //B. 在组装员工信息时, 还需要封装创建时间、修改时间，创建人、修改人信息(从session中获取当前登录用户)。


    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("开始新增员工");
        //将员工的初始密码设置为12345
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);
        //封装创建时间，修改时间，创建人，修改人信息

        //先从session当中获取修改人的信息
        //Long empId = (Long) request.getSession().getAttribute("employee");

        //注释掉这个操作
//       employee.setCreateUser(empId);
//       employee.setCreateTime(LocalDateTime.now());
//       employee.setUpdateTime(LocalDateTime.now());
//       employee.setUpdateUser(empId);
        employeeService.save(employee);

        //查看是否封装数据成功
        log.info("数据封装" + employee);
        return R.success("创建成功！");
    }

    @GetMapping("/page")
    public R<Page<Employee>> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, page, name);

        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        //添加排序条件
        //根据创建时间来查询
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }


    @PutMapping
    public R<Employee> update(HttpServletRequest request, @RequestBody Employee employee) {


        //获取当前登录的用户id
        // Long empId = (Long) request.getSession().getAttribute("employee");
        //设置修改时间和修改人


//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        log.info(employee.toString());

        //前端已经处理好相应的数据了，直接获取和修改就行
        employeeService.updateById(employee);
        return R.success(employee);
    }


    @GetMapping("/{id}")
    public R<Employee> edit(@PathVariable("id") Long id) {
        Employee emp = employeeService.getById(id);
        //测试是否可以正常数据回显
        log.info(emp.toString());
        return R.success(emp);
    }
}
