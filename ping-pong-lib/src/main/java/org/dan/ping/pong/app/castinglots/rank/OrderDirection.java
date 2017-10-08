package org.dan.ping.pong.app.castinglots.rank;

import java.util.Comparator;

public enum OrderDirection {
    Increase {
        @Override
        public <T> Comparator<T> setupOrder(Comparator<T> c) {
            return c;
        }
    },
    Decrease {
        @Override
        public <T> Comparator<T> setupOrder(Comparator<T> c) {
            return c.reversed();
        }
    };

    public abstract <T> Comparator<T> setupOrder(Comparator<T> c);
}
