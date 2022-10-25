package com.chenpeiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenpeiyu.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;


//交给spring管理
@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {

}
