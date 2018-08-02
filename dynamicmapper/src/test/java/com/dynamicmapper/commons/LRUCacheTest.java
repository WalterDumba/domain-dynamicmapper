package com.dynamicmapper.commons;

import org.junit.Assert;
import org.junit.Test;

public class LRUCacheTest {





    @Test
    public void testLRUCacheEviction(){


        LRUCache<Integer, String> cache = new LRUCache<>(4);


        cache.put(1,    "a");
        cache.put(2,    "b");
        cache.put(3,    "c");
        cache.put(4,    "d");

        //Access eldest one to keep it recently
        cache.get(1);
        //Next access will evict the cache
        cache.put(5,    "e");
        cache.put(6,    "f");

        //
        Assert.assertTrue( cache.get(1).equals("a") );
    }

}