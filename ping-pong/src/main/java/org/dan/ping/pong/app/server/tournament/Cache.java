package org.dan.ping.pong.app.server.tournament;

public interface Cache<K, V> {
    V load(K k);
    void invalidate(K k);
}
