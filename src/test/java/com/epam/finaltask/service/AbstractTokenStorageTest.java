package com.epam.finaltask.service;

import com.epam.finaltask.service.impl.AbstractTokenStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractTokenStorageTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private AbstractTokenStorage<String> storage;

    @BeforeEach
    void setUp() {
        lenient().when(cacheManager.getCache("test_cache")).thenReturn(cache);
        storage = new AbstractTokenStorage<>(cacheManager, "test_cache", String.class) {
        };
    }

    @Test
    @DisplayName("Constructor: Should throw IllegalArgumentException if cache is null")
    void constructor_ShouldThrowException_WhenCacheNotFound() {
        // Arrange
        when(cacheManager.getCache("non_existent")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> new AbstractTokenStorage<>(cacheManager, "non_existent", String.class) {
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cache non_existent not found");
    }

    @Test
    @DisplayName("store: Should put value into cache")
    void store_ShouldPutValueInCache() {
        // Act
        storage.store("key1", "value1");

        // Assert
        verify(cache).put("key1", "value1");
    }

    @Test
    @DisplayName("get: Should return casted value when cache hit")
    void get_CacheHit_ShouldReturnTypedValue() {
        // Arrange
        String expectedValue = "token_data";
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        when(wrapper.get()).thenReturn(expectedValue);
        when(cache.get("id1")).thenReturn(wrapper);

        // Act
        String result = storage.get("id1");

        // Assert
        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("get: Should return null when wrapper is null")
    void get_WrapperNull_ShouldReturnNull() {
        // Arrange
        when(cache.get("id1")).thenReturn(null);

        // Act
        String result = storage.get("id1");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("get: Should return null when value inside wrapper is null")
    void get_ValueInWrapperNull_ShouldReturnNull() {
        // Arrange
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        when(wrapper.get()).thenReturn(null);
        when(cache.get("id1")).thenReturn(wrapper);

        // Act
        String result = storage.get("id1");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("revoke: Should evict entry from cache")
    void revoke_ShouldEvictFromCache() {
        // Act
        storage.revoke("id_to_delete");

        // Assert
        verify(cache).evict("id_to_delete");
    }

    @Test
    @DisplayName("clearAll: Should clear entire cache")
    void clearAll_ShouldClearCache() {
        // Act
        storage.clearAll();

        // Assert
        verify(cache).clear();
    }
}