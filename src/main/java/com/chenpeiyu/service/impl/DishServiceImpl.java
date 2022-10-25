package com.chenpeiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpeiyu.dto.DishDto;
import com.chenpeiyu.entity.Dish;
import com.chenpeiyu.entity.DishFlavor;
import com.chenpeiyu.mapper.DishMapper;
import com.chenpeiyu.service.DishFlavorService;
import com.chenpeiyu.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    //用于保存flavor
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品同时有人保存对应的口味数据
     *
     * @param dishDto
     */

    //①. 保存菜品基本信息 ;
    //
    //②. 获取保存的菜品ID ;
    //
    //③. 获取菜品口味列表，遍历列表，为菜品口味对象属性dishId赋值;
    //
    //④. 批量保存菜品口味列表;
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //因为口味表当中有一个菜品id但是没有办法直接封装到表中，先取到菜品id
        Long dishid = dishDto.getId();


        //添加菜品口味

        List<DishFlavor> flavors = dishDto.getFlavors();

        //遍历数据获得每一个flavor实体 然后保存在口味表中
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishid);
            dishFlavorService.save(flavor);
        }
    }

    /**
     * 根据传入的id获得dishDto用于数据回显
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getDishDtoWithFlavor(Long id) {
        //获得dish的对象
        Dish dish = this.getById(id);

        //创建一个dishDto对象用来返回
        DishDto dishDto = new DishDto();

        //先将dish对象的数据先复制到dto对象中
        BeanUtils.copyProperties(dish, dishDto);

        //口味表中根据dish_id获取flavor数据

        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();

        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());

        List<DishFlavor> list = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

        dishDto.setFlavors(list);
        return dishDto;

    }


    @Override
    public void updateDishDtoWithFlavor(DishDto dishDto) {
        //开始保存两个表 首先先开启Transational注解

        //首先先开始更新dish表的基本信息
        this.updateById(dishDto);

        //删除原来保存的口味表数据
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();

        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);


        //添加当前提交过来的口味数据，dish_flavor表的insert操作

        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).toList();

        dishFlavorService.saveBatch(flavors);
    }


}
