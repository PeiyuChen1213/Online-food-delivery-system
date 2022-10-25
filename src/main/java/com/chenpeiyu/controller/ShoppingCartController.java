package com.chenpeiyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenpeiyu.common.BaseContext;
import com.chenpeiyu.common.R;
import com.chenpeiyu.entity.ShoppingCart;
import com.chenpeiyu.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {


    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查看购物车的数据
     *
     * @return 返回购物车的数据
     */

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        //根据user_id查询数据库查找相关的对象
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(BaseContext.getCurrentId() != null, ShoppingCart::getUserId, BaseContext.getCurrentId());

        //添加排序条件
        shoppingCartLambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        //开始查询数据
        List<ShoppingCart> list = shoppingCartService.list(shoppingCartLambdaQueryWrapper);

        return R.success(list);
    }

    //A. 获取当前登录用户，为购物车对象赋值
    //
    //B. 根据当前登录用户ID 及 本次添加的菜品ID/套餐ID，查询购物车数据是否存在
    //
    //C. 如果已经存在，就在原来数量基础上加1
    //
    //D. 如果不存在，则添加到购物车，数量默认就是1

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {

        //传入的数据当中，没有当前的用户id, 还要对商品数量的定义

        log.info("查看当前的数据" + shoppingCart);

        //获得当前的登录的用户，为当前的对象赋值
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //根据当前的菜品或者当前的套餐的id

        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(currentId != null, ShoppingCart::getUserId, shoppingCart.getUserId());

        //判断当前的菜品是套餐还是菜品

        if (shoppingCart.getDishId() != null) {
            //待加入的是菜品
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //加入的是套餐
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        if (shoppingCartServiceOne != null) {
            //如果存在的话 在原来的数量加上1 再更新数据库
            Integer number = shoppingCartServiceOne.getNumber();
            shoppingCartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        } else {
            //如果不存在的话，将数量设置为1 更新数据库
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //返回购物车数据
            shoppingCartServiceOne = shoppingCart;
        }
        return R.success(shoppingCartServiceOne);
    }


    /**
     * 清空当前的购物车，根据用户的id
     *
     * @return 成功的标志
     */
    @DeleteMapping("/clean")
    public R<String> emptyCart() {
        //查询购物车对应的表，将对应用户id下的所有记录全部删除
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(BaseContext.getCurrentId() != null, ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
        return R.success("清空成功！");
    }


    @PostMapping("/sub")
    public R<String> subCartNumber(@RequestBody ShoppingCart shoppingCart) {
        //传入的参数要么就是dish_id 或者setMeal_id

        //查询传入的菜品或者套餐的id
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();

        shoppingCartLambdaQueryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        shoppingCartLambdaQueryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        ShoppingCart record = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        //找到对应的数据库的记录，然后在原来的基础上减去1
        Integer number = record.getNumber();
        if (number - 1 > 0) {
            record.setNumber(number - 1);
            //更新数据库
            shoppingCartService.updateById(record);
        } else {
            //直接删除这条记录
            shoppingCartService.removeById(record);
        }

        return R.success("减少数量操作成功！");
    }

}
