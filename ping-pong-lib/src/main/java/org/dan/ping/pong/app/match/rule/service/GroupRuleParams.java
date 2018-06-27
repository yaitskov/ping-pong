package org.dan.ping.pong.app.match.rule.service;

import static org.dan.ping.pong.app.match.rule.filter.DisambiguationScope.ORIGIN_MATCHES;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.filter.DisambiguationScope;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
public class GroupRuleParams {
    private TournamentMemState tournament;
    private List<MatchInfo> groupMatches;
    private Set<Bid> bids;
    private int gid;
    private DisambiguationScope disambiguationMode;
    private boolean disambiguationMatchesWillBeCreated;
    private boolean countIncompleteMatches;
    private List<GroupOrderRule> orderRules;

    public static class GroupRuleParamsBuilder {
        DisambiguationScope disambiguationMode = ORIGIN_MATCHES;
    }

    public static GroupRuleParams ofParams(int gid,
            TournamentMemState tournament, List<MatchInfo> groupMatches,
            List<GroupOrderRule> orderRules,
            Set<Bid> bids) {
        if (bids.isEmpty()) {
            throw internalError("no uids");
        }
        return GroupRuleParams
                .builder()
                .bids(bids)
                .gid(gid)
                .tournament(tournament)
                .groupMatches(groupMatches)
                .orderRules(orderRules)
                .build();
    }
}
