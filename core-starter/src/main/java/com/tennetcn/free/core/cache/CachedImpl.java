package com.tennetcn.free.core.cache;

import com.tennetcn.free.core.properties.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 缓存实现
 *
 */
@Slf4j
@Component
public class CachedImpl implements ICached {

	@Autowired
	private CacheManager cacheManager;
	

	@Override
	public void put(String key, Object value) {
		log.info("cacheManager is {}",cacheManager.getClass().getName());
		cacheManager.getCache(CacheProperties.CACHE_NAME).put(key, value);
	}

	@Override
	public Object get(String key) {
		ValueWrapper valueWrapper = cacheManager.getCache(CacheProperties.CACHE_NAME).get(key);
		if(valueWrapper != null) {
			return valueWrapper.get();
		}else {
			return null;
		}
	}

	@Override
	public <T> T get(String key, Class<T> tClass) {
		Object object = get(key);
		if(object==null){
			return null;
		}
		return (T)object;
	}

	@Override
	public void remove(String key) {
		cacheManager.getCache(CacheProperties.CACHE_NAME).evict(key);
	}



}
