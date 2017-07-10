package org.dan.ping.pong.util.collection;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.primitives.Ints.asList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EnumeratorTest {
    @Test
    public void forward() {
        List<Integer> result = new ArrayList<>();
        addAll(result, Enumerator.forward(0, 4));
        assertEquals(asList(0, 1, 2, 3), result);
    }

    @Test
    public void backward() {
        List<Integer> result = new ArrayList<>();
        addAll(result, Enumerator.backward(4, 0));
        assertEquals(asList(3, 2, 1, 0), result);
    }
}
