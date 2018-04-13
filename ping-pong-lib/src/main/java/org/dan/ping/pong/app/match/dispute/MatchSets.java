package org.dan.ping.pong.app.match.dispute;

import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class MatchSets {
    private Map<Uid, List<Integer>> sets;

    public static MatchSets ofSets(Map<Uid, List<Integer>> sets) {
        return new MatchSets(sets);
    }

    public void validateNumberParticipants() {
        if (sets.size() != 2) {
            throw badRequest("Wrong number of participants in a match");
        }
    }

    public int validateEqualNumberSets() {
        final Iterator<Uid> uidIterator = sets.keySet().iterator();
        final Uid uid1 = uidIterator.next();
        final Uid uid2 = uidIterator.next();
        final List<Integer> score1 = sets.get(uid1);
        final List<Integer> score2 = sets.get(uid2);
        if (score1.size() != score2.size()) {
            throw badRequest("Set number mismatch");
        }
        return score1.size();
    }

    public void validateParticipants(Set<Uid> expected) {
        if (!expected.equals(sets.keySet())) {
            throw badRequest("match has different participants");
        }
    }
}
