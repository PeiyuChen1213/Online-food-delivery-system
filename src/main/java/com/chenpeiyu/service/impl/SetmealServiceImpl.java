package com.chenpeiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpeiyu.common.CustomException;
import com.chenpeiyu.dto.SetmealDto;
import com.chenpeiyu.entity.Setmeal;
import com.chenpeiyu.entity.SetmealDish;
import com.chenpeiyu.mapper.SetmealMapper;
import com.chenpeiyu.service.SetmealDishService;
import com.chenpeiyu.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveSetmealWithDish(SetmealDto setmealDto) {
        //先将setmeal的数据存到数据库当中去
        this.save(setmealDto);
        //分析数据库可以知道表中的数据还差一个传入的数据当中还差一个setmealID 接下来就是将所有的数据取出封装到一个新地集合当中然后再保存到数据库当中
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            SetmealDish setmealDish = new SetmealDish();
            //将获取的每一个的item的属性拷贝到setmealDish当中
            BeanUtils.copyProperties(item, setmealDish);
            //将setmealid这个关键属性注入进去
            //根据套餐名称查找这个套餐的名字
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).toList();
        //将setmealDishes的数据保存到数据库当中
        setmealDishService.saveBatch(setmealDishes);
    }


    @Override
    public SetmealDto getSetmealDtoWithDish(Long id) {
        //从数据库中查询对应id的数据
        Setmeal setmeal = this.getById(id);

        //创建一个dto对象
        SetmealDto setmealDto = new SetmealDto();

        //套餐数据复制到dto对象中
        BeanUtils.copyProperties(setmeal, setmealDto);

        //从套餐菜品这个数据库中查询出对应的菜品的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(lambdaQueryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);
        //为了避免循环依赖，就不直接在这里处理
        return setmealDto;
    }


    @Override
    public void updateSetmealDtoWithDish(SetmealDto setmealDto) {
        //操作你两张表
        //先更新setmeal的数据
        this.updateById(setmealDto);
        //删除旧的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lambdaQueryWrapper);

        //分析数据库可以知道表中的数据还差一个传入的数据当中还差一个setmealID 接下来就是将所有的数据取出封装到一个新地集合当中然后再保存到数据库当中
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            SetmealDish setmealDish = new SetmealDish();
            //将获取的每一个的item的属性拷贝到setmealDish当中
            BeanUtils.copyProperties(item, setmealDish);
            //将setmealid这个关键属性注入进去
            //根据套餐名称查找这个套餐的名字
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).toList();
        //将setmealDishes的数据保存到数据库当中
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void delete(List<Long> ids) {
        //测试数据是否可以正常传到后台
        log.info(ids.toString());

        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        int count = (int) this.count(queryWrapper);
        if (count > 0) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);

    }
}
