package com.chenpeiyu.dto;

import com.chenpeiyu.entity.Dish;
import com.chenpeiyu.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//创建一个dto的实体类扩展一个实体类

@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();
    private String categoryName;//用来保存分类名称的
    private Integer copies;
}
