package com.chenpeiyu.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用的返回结果，服务端的响应的数据都会被封装成此对象
 */

@Data
public class R<T> implements Serializable {
    private Integer code;//封装状态编码，如果1 代表成功，其他数字代表失败。这是事先之前约定好的

    private String msg;//错误信息

    private T data; //数据

    private Map map = new HashMap();//这是一个动态设置

    public static <T> R<T> success(T object) {
        R<T> r = new R<>();
        r.data = object;
        r.code = 1;
        return r;
    }


    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.data = null;
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
