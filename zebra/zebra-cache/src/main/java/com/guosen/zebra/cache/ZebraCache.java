/**   
* @Title: ZebraCacheManager.java 
* @Package com.guosen.zebra.core.cache 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年9月6日 下午4:41:49 
* @version V1.0   
*/
package com.guosen.zebra.cache;

import com.guosen.zebra.cache.change.publisher.CacheChangePublisher;
import com.guosen.zebra.cache.change.UpdateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.util.concurrent.Callable;

/**
 * @ClassName: ZebraCache
 * @Description: extends RedisCache，增加本地一级缓存，redis作为二级缓存
 * @author 邓启翔
 * @date 2018年9月6日 下午4:41:49
 * 
 */

public class ZebraCache extends RedisCache {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZebraCache.class);
	private final CaffeineCache caffeineCache;// 本地一级缓存
	private boolean enableCacheTwo;
	private CacheChangePublisher cacheChangePublisher;


	public ZebraCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig,
					  CaffeineCache caffeineCache,
					  CacheChangePublisher cacheChangePublisher) {
		super(name, cacheWriter, cacheConfig);
		this.caffeineCache = caffeineCache;
		this.cacheChangePublisher = cacheChangePublisher;
	}

	@Override  
	public synchronized <T> T get(Object key, Callable<T> valueLoader) {
		// 先读取本地一级缓存

		T value = null;
		try {
			value = (T) caffeineCache.get(key, valueLoader);
			if (value == null && isEnableCacheTwo()) {
				// 本地一级缓存不存在，读取redis二级缓存
				value = super.get(key, valueLoader);
				if (value != null) {
					// redis二级缓存存在，存入本地一级缓存
					caffeineCache.put(key, value);
					// 发布缓存更新消息通知其他client更新缓存
					pub(new UpdateMessage(key, value, UpdateMessage.Type.PUT));
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to get key : {}", key, e);
		}

		return value;
	}

	@Override
	public void put(Object key, Object value) {
		caffeineCache.put(key, value);
		if(isEnableCacheTwo()){
			super.put(key, value);
			pub(new UpdateMessage(key, value, UpdateMessage.Type.PUT));
		}
	}

	@Override  
    public ValueWrapper putIfAbsent(Object key, Object value) {  
		ValueWrapper vw1 = caffeineCache.putIfAbsent(key, value);  
        if(isEnableCacheTwo()){
        	 ValueWrapper vw2 = super.putIfAbsent(key, value);  
             pub(new UpdateMessage(key, value, UpdateMessage.Type.PUTIFABSENT)); 
             return vw2;
        }
        return vw1;  
    }

	@Override
	public void evict(Object key) {
		caffeineCache.evict(key);
		if(isEnableCacheTwo()){
			super.evict(key);
			pub(new UpdateMessage(key, UpdateMessage.Type.REMOVE));
		}
	}

	@Override
	public void clear() {
		caffeineCache.clear();
		if(isEnableCacheTwo()){
			super.clear();
			pub(new UpdateMessage(UpdateMessage.Type.CLEAN));
		}
	}

	@Override  
    public ValueWrapper get(Object key) {  
        ValueWrapper valueWrapper = caffeineCache.get(key);  
        if (valueWrapper == null && isEnableCacheTwo()) {  
            valueWrapper = super.get(key);  
            if (valueWrapper != null) {  
            	caffeineCache.put(key, valueWrapper.get());  
                pub(new UpdateMessage(key, valueWrapper.get(), UpdateMessage.Type.PUT));  
            }  
        }  
        return valueWrapper;  
    }

	@Override  
    public <T> T get(Object key, Class<T> type) {  
        T value = caffeineCache.get(key, type);  
        if (value == null && isEnableCacheTwo()) {  
            value = super.get(key, type);  
            if (value != null) {  
            	caffeineCache.put(key, value);  
                pub(new UpdateMessage(key, value, UpdateMessage.Type.PUT));  
            }  
        }  
        return value;  
    }

	/**
	 * 处理缓存更新消息
	 */
	public void handleForLocalCache(UpdateMessage updateMessage) {
		// 处理缓存更新消息通知，只更新一级缓存
		Object key = updateMessage.getKey();
		Object value = updateMessage.getValue();

		switch (updateMessage.getType()) {
			case CLEAN: {
				caffeineCache.clear();
				break;
			}
			case PUT: {
				caffeineCache.put(key, value);
				break;
			}
			case PUTIFABSENT: {
				caffeineCache.putIfAbsent(key, value);
				break;
			}
			case REMOVE: {
				caffeineCache.evict(key);
				break;
			}
			default: {
				// do nothing
				break;
			}
		}
	}

	public void handleForRedisCache(UpdateMessage updateMessage) {
		Object key = updateMessage.getKey();
		Object value = updateMessage.getValue();

		switch (updateMessage.getType()) {
			case CLEAN: {
				super.clear();
				break;
			}
			case PUT: {
				super.put(key, value);
				break;
			}
			case PUTIFABSENT: {
				super.putIfAbsent(key, value);
				break;
			}
			case REMOVE: {
				super.evict(key);
				break;
			}
			default: {
				// do nothing
				break;
			}
		}
	}

	/**
	 * 通知其他微服务实例更新缓存
	 * 
	 * @param message
	 */
	private void pub(final UpdateMessage message) {
		// 统一设置下缓存名称
		message.setCacheName(getName());

		cacheChangePublisher.publish(message);
	}

	public boolean isEnableCacheTwo() {
		return enableCacheTwo;
	}

	public void setEnableCacheTwo(boolean enableCacheTwo) {
		this.enableCacheTwo = enableCacheTwo;
	}

}
