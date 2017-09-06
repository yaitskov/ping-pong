package org.dan.ping.pong.app.group;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.match.MatchState.Over;

import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.MatchValidationRule;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GroupService {
    public Map<Integer, BidSuccessInGroup> emptyMatchesState(
            OpenTournamentMemState tournament,
            Collection<MatchInfo> allMatchesInGroup) {
        checkArgument(allMatchesInGroup.stream()
                .allMatch(minfo -> minfo.getState() == Over));
        return allMatchesInGroup.stream()
                .map(MatchInfo::getParticipantIdScore)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(toMap(uid -> uid,
                        uid -> new BidSuccessInGroup(uid, tournament.getParticipant(uid).getState())));
    }

    public List<Integer> orderUidsInGroup(OpenTournamentMemState tournament,
            List<MatchInfo> allMatchesInGroup) {
        final Map<Integer, BidSuccessInGroup> uid2Stat = emptyMatchesState(tournament, allMatchesInGroup);
        final MatchValidationRule matchRule = tournament.getRule().getMatch();
        allMatchesInGroup.forEach(minfo -> aggMatch(uid2Stat, minfo, matchRule));
        return order(uid2Stat.values())
                .stream().map(BidSuccessInGroup::getUid)
                .collect(toList());
    }

    public Collection<BidSuccessInGroup> order(Collection<BidSuccessInGroup> bidSuccess) {
        return bidSuccess.stream().sorted(BidSuccessInGroup.BEST_COMPARATOR).collect(toList());
    }

    public void aggMatch(Map<Integer, BidSuccessInGroup> uid2Stat,
            MatchInfo minfo, MatchValidationRule matchRule) {
        final int winUid = minfo.getWinnerId().get();
        final BidSuccessInGroup winner = uid2Stat.get(winUid);
        final int lostUid = minfo.getOpponentUid(winUid).get();
        final BidSuccessInGroup loser = uid2Stat.get(lostUid);

        minfo.getParticipantIdScore().get(winUid)
                .forEach(winner::winBalls);
        minfo.getParticipantIdScore().get(winUid)
                .forEach(loser::lostBalls);
        minfo.getParticipantIdScore().get(lostUid)
                .forEach(loser::winBalls);
        minfo.getParticipantIdScore().get(lostUid)
                .forEach(winner::lostBalls);

        final Map<Integer, Integer> uid2Sets = matchRule.calcWonSets(minfo);

        loser.wonSets(uid2Sets.get(lostUid));
        loser.lostSets(uid2Sets.get(winUid));
        winner.wonSets(uid2Sets.get(winUid));
        winner.lostSets(uid2Sets.get(lostUid));

        winner.win();
        matchRule.findWinnerId(uid2Sets)
                .ifPresent(uid -> loser.lost()); // walkover = 0
    }
}
