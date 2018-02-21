package org.dan.ping.pong.app.group;

import lombok.Getter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.List;
import java.util.Map;

@Getter
public class OrderUidsInGroupCmd {
    private final List<MatchInfo> matches;
    private final Map<Uid, Integer> uid2Points;
    private final ExtraUidOrderInGroup strongerExtraOrder;
    private final List<Uid> finalUidsOrder;

    public OrderUidsInGroupCmd(TournamentMemState tournament, int gid, GroupService groupService) {
        matches = groupService.findMatchesInGroup(tournament, gid);
        uid2Points = groupService.countPoints(tournament, matches);
        strongerExtraOrder = groupService.findStrongerExtraOrder(tournament, uid2Points, matches);
        finalUidsOrder = groupService.orderUidsByPointsAndExtraOrder(uid2Points,
                strongerExtraOrder.getStrongerOf());
    }
}
