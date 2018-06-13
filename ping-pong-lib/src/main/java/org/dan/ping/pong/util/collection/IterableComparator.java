package org.dan.ping.pong.util.collection;

import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.Iterator;

@RequiredArgsConstructor
public class IterableComparator<T> {
    private final Comparator<T> comparator;

    public enum LengthPolicy {
        LongerSmaller {
            public int longer() {
                return -1;
            }
            public int shorter() {
                return 1;
            }
        };

        public abstract int longer();
        public abstract int shorter();
    }

    public int compare(Iterable<T> a, Iterable<T> b, LengthPolicy policy) {
        final Iterator<T> aIter = a.iterator();
        final Iterator<T> bIter = b.iterator();
        while (aIter.hasNext() && bIter.hasNext()) {
            final int cmp = comparator.compare(aIter.next(), bIter.next());
            if (cmp != 0) {
                return cmp;
            }
        }
        if (aIter.hasNext()) {
            return policy.longer();
        } else if (bIter.hasNext()) {
            return policy.shorter();
        }
        return 0;
    }
}
