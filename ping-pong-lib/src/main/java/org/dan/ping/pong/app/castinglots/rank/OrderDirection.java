package org.dan.ping.pong.app.castinglots.rank;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;

public enum OrderDirection {
    @JsonProperty("i")
    Increase {
        @Override
        public <T> Comparator<T> setupOrder(Comparator<T> c) {
            return c;
        }
    },
    @JsonProperty("d")
    Decrease {
        @Override
        public <T> Comparator<T> setupOrder(Comparator<T> c) {
            return c.reversed();
        }
    };

    public abstract <T> Comparator<T> setupOrder(Comparator<T> c);
}
