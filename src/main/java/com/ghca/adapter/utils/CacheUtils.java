package com.ghca.adapter.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/1/22 13:43
 */

public class CacheUtils {

    private static Cache<Object, Object> cache = Caffeine.newBuilder().initialCapacity(10).maximumSize(1000).expireAfterWrite(24,
        TimeUnit.HOURS).build();

    public static Cache<Object, Object> getCache(){
        return cache;
    }
}
