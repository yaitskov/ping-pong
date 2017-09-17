package org.dan.ping.pong.util.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public interface StreamsP {
    static <T> Stream<T> asStream(Iterator<T> itr) {
        List<T> result = new ArrayList<>();
        while (itr.hasNext()) {
            result.add(itr.next());
        }
        return result.stream();
    }
}
