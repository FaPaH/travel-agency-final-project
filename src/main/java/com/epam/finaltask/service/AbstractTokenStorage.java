package com.epam.finaltask.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public abstract class AbstractTokenStorage<T> implements TokenStorageService<T> {

    private final Cache cache;
    private final Class<T> type;

    protected AbstractTokenStorage(CacheManager cacheManager, String cacheName, Class<T> type) {
        this.cache = cacheManager.getCache(cacheName);
        this.type = type;
        if (cache == null) {
            throw new IllegalArgumentException("Cache " + cacheName + " not found");
        }
    }

    @Override
    public void store(String id, T token) {
        cache.put(id, token);
    }

    @Override
    public T get(String id) {
        var wrapper = cache.get(id);
        if (wrapper == null || wrapper.get() == null) {
            return null;
        }
        return type.cast(wrapper.get());
    }

    @Override
    public void revoke(String id) {
        cache.evict(id);
    }
}
