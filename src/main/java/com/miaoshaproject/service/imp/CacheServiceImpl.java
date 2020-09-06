package com.miaoshaproject.service.imp;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miaoshaproject.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private Cache<String,Object> commonCache=null;

    @PostConstruct
    public void init(){
        commonCache= CacheBuilder.newBuilder()
                //缓存容器初始容量为10
                .initialCapacity(10)
                //设置缓存最大容量，超过按照LRU策略移除
                .maximumSize(100)
                //设置写缓存后多长时间过期
                .expireAfterWrite(60, TimeUnit.SECONDS).build();
    }

    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key,value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
