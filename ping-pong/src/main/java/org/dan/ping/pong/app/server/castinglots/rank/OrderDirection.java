package org.dan.ping.pong.app.server.castinglots.rank;

import ord.dan.ping.pong.jooq.tables.records.BidRecord;
import org.jooq.SortField;
import org.jooq.TableField;

import java.util.Comparator;
import java.util.Optional;

public enum OrderDirection {
    Increase {
        @Override
        public <T> Comparator<T> setupOrder(Comparator<T> c) {
            return c;
        }

        @Override
        public SortField<Optional<Integer>> setupOrder(TableField<BidRecord, Optional<Integer>> field) {
            return field.asc();
        }
    },
    Decrease {
        @Override
        public <T> Comparator<T> setupOrder(Comparator<T> c) {
            return c.reversed();
        }

        @Override
        public SortField<Optional<Integer>> setupOrder(TableField<BidRecord, Optional<Integer>> field) {
            return field.desc();
        }
    };

    public abstract <T> Comparator<T> setupOrder(Comparator<T> c);

    public abstract SortField<Optional<Integer>> setupOrder(
            TableField<BidRecord, Optional<Integer>> field);
}
