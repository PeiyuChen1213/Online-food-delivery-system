package com.chenpeiyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpeiyu.dto.SetmealDto;
import com.chenpeiyu.entity.Setmeal;

import java.util.List;

/*使用mybatis——plus自动封装好的service*/
public interface SetmealService extends IService<Setmeal> {
    public void saveSetmealWithDish(SetmealDto setmealDto);

    public SetmealDto getSetmealDtoWithDish(Long id);

    public void updateSetmealDtoWithDish(SetmealDto setmealDto);

    public void delete(List<Long> ids);
}
