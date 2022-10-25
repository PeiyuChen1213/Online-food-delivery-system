package com.chenpeiyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpeiyu.dto.DishDto;
import com.chenpeiyu.entity.Dish;

/*使用mybatis——plus自动封装好的service*/
public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);


    public DishDto getDishDtoWithFlavor(Long id);

    public void updateDishDtoWithFlavor(DishDto dishDto);
}
