package com.chenpeiyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpeiyu.common.CustomException;
import com.chenpeiyu.common.R;
import com.chenpeiyu.dto.DishDto;
import com.chenpeiyu.entity.Category;
import com.chenpeiyu.entity.Dish;
import com.chenpeiyu.entity.DishFlavor;
import com.chenpeiyu.service.CategoryService;
import com.chenpeiyu.service.DishFlavorService;
import com.chenpeiyu.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    //将service的方法注入

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    //实现分页功能的方法

    @GetMapping("/page")
    public R<Page<DishDto>> page(Long page, Long pageSize, String name) {
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //查看日志里的记录
        log.info("pageInfo" + pageInfo.getRecords().toString());

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);

        log.info(dishDtoPage.getRecords().toString());
        return R.success(dishDtoPage);
    }
    //新增菜品的方法

    @PostMapping
    public R<String> addDishes(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        //service的实现类上封装一个方法
        dishService.saveWithFlavor(dishDto);
        return R.success("新增成功！");
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDtoWithFlavor = dishService.getDishDtoWithFlavor(id);
        return R.success(dishDtoWithFlavor);
    }


    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateDishDtoWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    //批量删除菜品


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {

        //从表中根据分类id来查询相关的菜品 传入的参数是种类id和售卖状态
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId()).eq(dish.getStatus() != null, Dish::getStatus, dish.getStatus());

        //添加模糊查询的相关条件
        dishLambdaQueryWrapper.like(dish.getName() != null, Dish::getName, dish.getName());
        //添加排序
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(dishLambdaQueryWrapper);
        //获取到当前的菜品列表后，处理它加上口味数据为止
        List<DishDto> list = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            //根据id查询分类对象
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                String categoryName = category.getName();
                //将种类的名称也添加上去
                dishDto.setCategoryName(categoryName);
            }

            //开始根据菜品的id进行查找对应的口味数据
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(item.getId() != null, DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).toList();
        return R.success(list);
    }


    //批量删除的方法
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        //根据id删除菜品数据，先判断在状态
        //先查询菜单的状态
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId, ids).eq(Dish::getStatus, 1);
        long count = dishService.count(dishLambdaQueryWrapper);
        //说明选中的数据当中有数据是处于起售的状态的
        if (count > 0) {
            throw new CustomException("在品还是在售状态！不可删除");
        }
        dishService.removeBatchByIds(ids);
        //在根据dish——id从口味当中删除数据
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
        return R.success("删除成功！");
    }


    //状态的改变
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        //起售前端传入的数据是1，拿到1之后 创建一个对象
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Dish::getId, ids);
        Dish dish = new Dish();
        dish.setStatus(status);
        dishService.update(dish, lambdaQueryWrapper);
        return R.success("修改成功！");
    }
}
