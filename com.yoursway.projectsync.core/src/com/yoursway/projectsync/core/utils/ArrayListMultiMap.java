package com.yoursway.projectsync.core.utils;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayListMultiMap<K, V> extends MultiMap<K, V> {
    
    @Override
    protected Collection<V> createInnerCollection() {
        return new ArrayList<V>();
    }
    
    public static <K, V> MultiMap<K, V> newArrayListMultiMap() {
        return new ArrayListMultiMap<K, V>();
    }
    
}
