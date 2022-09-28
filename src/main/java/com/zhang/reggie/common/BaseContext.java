package com.zhang.reggie.common;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取当前登入用户id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static long getCurrentId(){
        return threadLocal.get();
    }
}
