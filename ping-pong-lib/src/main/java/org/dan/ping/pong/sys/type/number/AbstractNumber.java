package org.dan.ping.pong.sys.type.number;

import static java.lang.Integer.compare;
import static java.lang.String.valueOf;

import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

public abstract class AbstractNumber
        extends Number
        implements Comparable<AbstractNumber>, Hashable {

    @Override
    public long longValue() {
        return intValue();
    }

    @Override
    public float floatValue() {
        return intValue();
    }

    @Override
    public double doubleValue() {
        return intValue();
    }

    @Override
    public int compareTo(AbstractNumber o) {
        return compare(intValue(), o.intValue());
    }

    @Override
    public void hashTo(HashAggregator sink) {
        sink.hash(intValue());
    }

    public String toString() {
        return valueOf(intValue());
    }
}
