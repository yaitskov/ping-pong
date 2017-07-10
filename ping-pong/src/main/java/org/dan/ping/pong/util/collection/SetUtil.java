package org.dan.ping.pong.util.collection;

import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SetUtil {
    public static <T> List<T> firstN(int n, Iterator<T> iterator) {
        List<T> result = new ArrayList<T>(n);
        Iterators.addAll(result, Iterators.limit(iterator, n));
        return result;
    }
}
