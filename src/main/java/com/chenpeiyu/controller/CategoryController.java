package com.chenpeiyu.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpeiyu.common.R;
import com.chenpeiyu.entity.Category;
import com.chenpeiyu.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //分页查询的方法
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize) {
        log.info("page={},pageSize={},name={}", page, page);
        //构造分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件
        //根据排序来查询
        queryWrapper.orderByAsc(Category::getSort);

        //执行查询
        categoryService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping
    public R<String> save(@RequestBody Category category) {
        boolean save = categoryService.save(category);
        if (save) {
            return R.success("添加成功！");
        } else {
            return R.error("添加失败");
        }
    }

    @PutMapping
    public R<Category> update(@RequestBody Category category) {
        boolean update = categoryService.updateById(category);
        if (update) {
            return R.success(category);
        } else {
            return R.error("修改失败!");
        }
    }

    @DeleteMapping
    public R<String> delete(Long ids) {
        boolean remove = categoryService.remove(ids);
        if (remove) {
            return R.success("删除成功！");
        } else {
            return R.error("删除失败");
        }
    }

    //查询菜品分类
    //请求参数，是type=1 返回值是一个菜品分类的集合

    @GetMapping("/list")
    public R<List<Category>> showCategory(Integer type) {
        //在数据库当中查询根据相关的条件查询
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //注意有可能传入的类型id值可能是空的
        categoryLambdaQueryWrapper.eq(type != null, Category::getType, type);
        //添加排序条件 根据更新时间降序来排序
        categoryLambdaQueryWrapper.orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(categoryLambdaQueryWrapper);
        return R.success(list);
    }

}
