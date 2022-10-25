package com.chenpeiyu.dto;

import com.chenpeiyu.entity.Setmeal;
import com.chenpeiyu.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
