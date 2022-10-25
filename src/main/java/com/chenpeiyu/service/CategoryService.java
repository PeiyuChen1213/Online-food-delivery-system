package com.chenpeiyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpeiyu.entity.Category;

public interface CategoryService extends IService<Category> {

    public boolean remove(Long id);
}
