package com.tennetcn.free.core.cache;

/**
 * @author chfree
 * @email chfree001@gmail.com
 * @create 2019-08-25 09:41
 * @comment
 */

public interface ICached {
    void put(String key,Object value);

    Object get(String key);

    <T> T get(String key,Class<T> tClass);

    void remove(String key);
}
