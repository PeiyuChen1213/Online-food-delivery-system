package com.chenpeiyu.common;

/**
 * 通过threadlocal这个类来处理登录的id的获取
 * 原因是：同一个请求，在过滤器，在控制器和元对象处理器上的线程都是同一个
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //设置值
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    //获取值
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
