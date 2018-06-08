package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;
import static org.dan.ping.pong.app.group.GroupService.MATCH_TAG_DISAMBIGUATION;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

@Getter
@EqualsAndHashCode
public class MatchParticipants implements Comparable<MatchParticipants>, Hashable {
    public static final Comparator<MatchParticipants> MATCH_PARTICIPANTS_COMPARATOR = Comparator
            .comparing(MatchParticipants::getUidLess)
            .thenComparing(MatchParticipants::getUidMore);

    private final Uid uidLess;
    private final Uid uidMore;

    public MatchParticipants(Uid uidLess, Uid uidMore) {
        if (uidLess.equals(uidMore)) {
            throw internalError("match uids equal " + uidLess);
        }
        if (uidLess.compareTo(uidMore) < 0) {
            this.uidLess = uidLess;
            this.uidMore = uidMore;
        } else {
            this.uidLess = uidMore;
            this.uidMore = uidLess;
        }
    }

    public static MatchParticipants create(MatchInfo match) {
        if (match.uids().size() != 2) {
            throw internalError("group match doesn't have enough participants");
        }
        final Iterator<Uid> a = match.uids().iterator();
        return new MatchParticipants(a.next(), a.next());
    }

    @Override
    public int compareTo(MatchParticipants o) {
        return MATCH_PARTICIPANTS_COMPARATOR.compare(this, o);
    }

    @Override
    public void hashTo(HashAggregator sink) {
        sink.hash(uidLess).hash(uidMore);
    }

    public MatchInfo toFakeMatch() {
        return MatchInfo.builder().mid(Mid.of(-1))
                .tag(MATCH_TAG_DISAMBIGUATION)
                .participantIdScore(
                        ImmutableMap.of(
                                uidLess, emptyList(),
                                uidMore, emptyList()))
                .build();
    }

    public boolean hasAll(Set<Uid> uids) {
        return uids.contains(uidLess) && uids.contains(uidMore);
    }
}
