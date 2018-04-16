package org.dan.ping.pong.app.match.rule.service;

import static java.util.Collections.emptySet;
import static org.dan.ping.pong.app.match.rule.filter.DisambiguationScope.ORIGIN_MATCHES;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.filter.DisambiguationScope;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.error.PiPoEx;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
public class GroupRuleParams {
    private TournamentMemState tournament;
    private List<MatchInfo> groupMatches;
    private Set<Uid> uids;
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
            Set<Uid> uids) {
        if (uids.isEmpty()) {
            throw internalError("no uids");
        }
        return GroupRuleParams
                .builder()
                .uids(uids)
                .gid(gid)
                .tournament(tournament)
                .groupMatches(groupMatches)
                .orderRules(orderRules)
                .build();
    }
}
