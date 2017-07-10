package org.dan.ping.pong.util.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Iterator;

public interface Enumerator {
    @Builder
    class ForwardIterable implements Iterable<Integer> {
        private final int start;
        private final int end;

        @Override
        public Iterator<Integer> iterator() {
            return new ForwardIterator(start, end);
        }
    }

    @AllArgsConstructor
    class ForwardIterator implements Iterator<Integer> {
        private int start;
        private int end;

        @Override
        public boolean hasNext() {
            return start < end;
        }

        @Override
        public Integer next() {
            return start++;
        }
    }

    @Builder
    class BackwardIterable implements Iterable<Integer> {
        private final int start;
        private final int end;

        @Override
        public Iterator<Integer> iterator() {
            return new BackwardIterator(start, end);
        }
    }

    @AllArgsConstructor
    class BackwardIterator implements Iterator<Integer> {
        private int start;
        private int end;

        @Override
        public boolean hasNext() {
            return start >= end;
        }

        @Override
        public Integer next() {
            return --start;
        }
    }

    static Iterable<Integer> forward(int start, int end) {
        return ForwardIterable.builder().start(start).end(end).build();
    }

    static Iterable<Integer> backward(int start, int end) {
        return BackwardIterable.builder().start(start).end(end + 1).build();
    }
}
