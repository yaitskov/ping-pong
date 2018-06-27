package org.dan.ping.pong.app.match.dispute;

import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class MatchSets {
    private Map<Bid, List<Integer>> sets;

    public static MatchSets ofSets(Map<Bid, List<Integer>> sets) {
        return new MatchSets(sets);
    }

    public void validateNumberParticipants() {
        if (sets.size() != 2) {
            throw badRequest("Wrong number of participants in a match");
        }
    }

    public int validateEqualNumberSets() {
        final Iterator<Bid> bidIterator = sets.keySet().iterator();
        final Bid bid1 = bidIterator.next();
        final Bid bid2 = bidIterator.next();
        final List<Integer> score1 = sets.get(bid1);
        final List<Integer> score2 = sets.get(bid2);
        if (score1.size() != score2.size()) {
            throw badRequest("Set number mismatch");
        }
        return score1.size();
    }

    public void validateParticipants(Set<Bid> expected) {
        if (!expected.equals(sets.keySet())) {
            throw badRequest("match has different participants");
        }
    }
}
