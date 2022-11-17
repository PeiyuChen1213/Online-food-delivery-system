package com.chenpeiyu.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpeiyu.common.R;
import com.chenpeiyu.dto.SetmealDto;
import com.chenpeiyu.entity.Category;
import com.chenpeiyu.entity.Setmeal;
import com.chenpeiyu.service.CategoryService;
import com.chenpeiyu.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    //注入需要使用的setmeal的相关service方法
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    //分页的对应方法
    //请求 URL: http://localhost:8080/setmeal/page?page=1&pageSize=10 get

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(Integer page, Integer pageSize, String name) {
        //创建分页对象
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);

        Page<SetmealDto> setmealDtoPage = new Page<>();

        //分析前端数据可以知道，如果直接使用Setmeal作为返回的类型，则会导致信息显示不全，
        //因为这个类的属性当中没有categoryName这个属性


        //开始查询数据库

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        setmealLambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);

        setmealService.page(setmealPage, setmealLambdaQueryWrapper);

        //将Setmeal的数据拷贝到数据库当中去 除了record记录
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        //获取原来分页对象当中的records数据，然后经过加工后再set到新的分页对象当中

        List<Setmeal> records = setmealPage.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            //将遍历到的每一个元素的都拷贝到新的对象当中去
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //然后在获取分类的名称注入到dto对象里面
            String categoryName = categoryService.getById(item.getCategoryId()).getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).toList();

        //将结果注入page分页对象当中
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    //新增套餐的方法

    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveSetmealWithDish(setmealDto);
        return R.success("添加成功！");
    }

    //套餐的数据回显
    @GetMapping("/{id}")
    public R<SetmealDto> getSetmeal(@PathVariable Long id) {
        SetmealDto setmealDtoWithDish = setmealService.getSetmealDtoWithDish(id);
        Category category = categoryService.getOne(new LambdaQueryWrapper<Category>().eq(Category::getId, setmealDtoWithDish.getCategoryId()));
        setmealDtoWithDish.setCategoryName(category.getName());
        return R.success(setmealDtoWithDish);
    }

    //套餐数据的修改
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> updateSetmeal(@RequestBody SetmealDto setmealDto) {
        setmealService.updateSetmealDtoWithDish(setmealDto);
        return R.success("修改成功！");
    }

    //套餐数据的删除实现

    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.delete(ids);
        return R.success("修改成功！");
    }

    //修改商品的起售或是停售
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        //起售前端传入的数据是1，拿到1之后 创建一个对象
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmealService.update(setmeal, setmealLambdaQueryWrapper);
        return R.success("修改成功！");
    }

    //前台页面发送套餐的请求

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
