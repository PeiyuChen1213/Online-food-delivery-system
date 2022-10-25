package com.chenpeiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpeiyu.common.CustomException;
import com.chenpeiyu.entity.Category;
import com.chenpeiyu.entity.Dish;
import com.chenpeiyu.entity.Setmeal;
import com.chenpeiyu.mapper.CategoryMapper;
import com.chenpeiyu.service.CategoryService;
import com.chenpeiyu.service.DishService;
import com.chenpeiyu.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    //注入需要的service方法获得

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 自己写的根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public boolean remove(Long id) {


        //添加查询条件，根据分类id进行查询菜品数据

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);


        long count = dishService.count(dishLambdaQueryWrapper);

        if (count > 0) {
            //如果关联了菜品直接报一个异常
            throw new CustomException("当前分类关联菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);

        long count1 = setmealService.count(setmealLambdaQueryWrapper);


        if (count1 > 0) {
            //同样的如果上面的分类和套餐关联了，也直接抛出一个异常
            throw new CustomException("当前分类与套餐相关联，无法删除");

        }


        //正常删除分类

        return super.removeById(id);
    }
}
