package com.chenpeiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenpeiyu.entity.User;
import org.apache.ibatis.annotations.Mapper;


/*mybatisPlus的自动封装好的接口*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
