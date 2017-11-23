package org.dan.ping.pong.util;

public interface TriFunc<A, B, C, R> {
    R apply(A a, B b, C c);
}
