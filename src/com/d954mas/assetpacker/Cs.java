package com.d954mas.assetpacker;

import java.util.*;


public class Cs {
    //region To strings

    //region Creators
    public static <T> List<T> of(Iterable<T> ts) {
        List<T> list = new ArrayList<>();
        for (T t : ts) {
            list.add(t);
        }
        return list;
    }

    @SafeVarargs
    public static <T> List<T> of(T... ts) {
        return new ArrayList<>(Arrays.asList(ts));
    }

    @SafeVarargs
    public static <T> Queue<T> ofQ(T... ts) {
        return new LinkedList<>(Arrays.asList(ts));
    }

    @SafeVarargs
    public static <T> Set<T> ofS(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    /**
     * odd - key, even - value
     */
    public static <K, V> Map<K, V> ofM(Object... ts) {
        Map<K, V> ret = new HashMap<>();
        int step = 0;
        K key = null;
        V val = null;
        for (Object t : ts) {
            if (step == 0) {
                key = (K) t;
                step++;
            } else if (step == 1) {
                val = (V) t;
                ret.put(key, val);
                step = 0;
            }
        }
        return ret;
    }

    //endregion
}
