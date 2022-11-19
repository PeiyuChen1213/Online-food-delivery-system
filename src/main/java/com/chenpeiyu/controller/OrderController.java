package com.chenpeiyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpeiyu.common.R;
import com.chenpeiyu.dto.OrdersDto;
import com.chenpeiyu.entity.OrderDetail;
import com.chenpeiyu.entity.Orders;
import com.chenpeiyu.service.OrderDetailService;
import com.chenpeiyu.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);
        return R.success("下单成功！");
    }


    /**
     * 后台管理订单信息分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, String number, String beginTime, String endTime) {
        log.info("page = {},pageSize = {}", page, pageSize);

        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(number != null, Orders::getId, number);
        queryWrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
        queryWrapper.le(endTime != null, Orders::getOrderTime, endTime);
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //执行查询
        ordersService.page(pageInfo, queryWrapper);

        List<Orders> records = pageInfo.getRecords();
        records = records.stream().map((item) -> {

            item.setUserName("用户" + item.getUserId());

            return item;
        }).collect(Collectors.toList());

        pageInfo.setRecords(records);
        return R.success(pageInfo);
    }

    /**
     * 派送订单
     *
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> updateOrder(@RequestBody Orders orders) {
        //构造条件构造器
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        //添加过滤条件
        updateWrapper.eq(Orders::getId, orders.getId());
        updateWrapper.set(Orders::getStatus, orders.getStatus());
        ordersService.update(updateWrapper);

        return R.success("订单派送成功");
    }


    @Transactional
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page, int pageSize, HttpServletRequest request) {
        log.info("page = {},pageSize = {}", page, pageSize);
        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //同时要根据用户的id来查看订单
        queryWrapper.eq(Orders::getUserId,request.getSession().getAttribute("user"));
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime,Orders::getStatus);
        //执行查询
        ordersService.page(pageInfo, queryWrapper);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,ordersDtoPage);
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> ordersDtoList = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            //根据订单的id去查orderDetail表中的数据，将每个数据的number累加起来就是份数
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            //查询条件
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,item.getNumber());
            //开始查询
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);
            //遍历每个订单的orderdetail对象得到对应的number，累加起来(前端已经实现了相关的功能)
//            int number = 0;
//            for (OrderDetail orderDetail : orderDetailList) {
//                number += orderDetail.getNumber();
//            }
//            ordersDto.setSumNum(number);
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersDtoPage);
    }


    /**
     * 再来一单
     * @param order //传入的参数是订单号
     * @return
     */
    @PostMapping("/again")
    @Transactional
    public R<String> again(@RequestBody Orders order){
        //传入的参数是一个json类型的数据
        //可以先去订单的数据库当中，把这个订单查出来，只要修改订单的下单时间就可以了
        Orders order1 = ordersService.getById(order.getId());

        //创建一个新的对象将这个数据拷贝到这里
        Orders order2 = new Orders();
        BeanUtils.copyProperties(order1,order2);
        Long id = IdWorker.getId();
        //更新下单的时间和订单表的主键id 和订单号 订单状态
        order2.setId(id);
        order2.setOrderTime(LocalDateTime.now());
        order2.setCheckoutTime(LocalDateTime.now());
        order2.setNumber(String.valueOf(id));
        order2.setStatus(2);

        //将数据保存到数据库当中
        ordersService.save(order2);

        //修改订单明细表的数据
        //先从数据库中将所有和这个订单号相关联的数据取出来

        LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderDetail::getOrderId,order.getId());
        List<OrderDetail> orderDetailList = orderDetailService.list(lambdaQueryWrapper);

        //只要修改orderdetail的订单号 同时再改变其雪花算法生成的id，不能用之前的id
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(id);
            orderDetail.setId(IdWorker.getId());
            //将这一条数据保存到数据库当中
            orderDetailService.save(orderDetail);
        }
        return R.success("再来一单！");
    }



}
