package org.dan.ping.pong.app.group;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.SetMultimap;
import lombok.Builder;
import org.dan.ping.pong.app.bid.Uid;

import java.util.Comparator;
import java.util.Map;

@Builder
public class UidGroupComparator implements Comparator<Uid> {
    private final Map<Uid, Integer> uid2Points;
    private final SetMultimap<Uid, Uid> strongerExtraOrder;

    @Override
    public int compare(Uid uid1, Uid uid2) {
        final int points1 = ofNullable(uid2Points.get(uid1))
                .orElseThrow(() -> internalError("no points for uid " + uid1));
        final int points2 = ofNullable(uid2Points.get(uid2))
                .orElseThrow(() -> internalError("no points for uid " + uid2));
        final int pointResult = Integer.compare(points2, points1); // reverse
        if (pointResult != 0) {
            return pointResult;
        }
        if (strongerExtraOrder.get(uid1).contains(uid2)) {
            return -1;
        }
        if (strongerExtraOrder.get(uid2).contains(uid1)) {
            return 1;
        }
        throw internalError("uid " + uid1 + " and " + uid2 + " are not ordered");
    }
}
