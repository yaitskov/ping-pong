package org.dan.ping.pong.app.group;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.match.MatchState.Over;

import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchValidationRule;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.Uid;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class GroupService {
    public Map<Uid, BidSuccessInGroup> emptyMatchesState(
            OpenTournamentMemState tournament,
            Collection<MatchInfo> allMatchesInGroup) {
        return allMatchesInGroup.stream()
                .map(MatchInfo::getParticipantIdScore)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(toMap(uid -> uid,
                        uid -> new BidSuccessInGroup(uid, tournament.getParticipant(uid).getState()),
                        (a, b) -> a));
    }

    public List<Uid> orderUidsInGroup(OpenTournamentMemState tournament,
            List<MatchInfo> allMatchesInGroup) {
        final Map<Uid, BidSuccessInGroup> uid2Stat = emptyMatchesState(tournament, allMatchesInGroup);
        final MatchValidationRule matchRule = tournament.getRule().getMatch();
        allMatchesInGroup.forEach(minfo -> aggMatch(uid2Stat, minfo, matchRule));
        return order(uid2Stat.values(), tournament.getRule().getGroup().get().getDisambiguation())
                .stream().map(BidSuccessInGroup::getUid)
                .collect(toList());
    }

    public Collection<BidSuccessInGroup> order(
            Collection<BidSuccessInGroup> bidSuccess,
            DisambiguationPolicy disambiguation) {
        return bidSuccess.stream()
                .sorted(disambiguation.getComparator())
                .collect(toList());
    }

    public void aggMatch(Map<Uid, BidSuccessInGroup> uid2Stat,
            MatchInfo minfo, MatchValidationRule matchRule) {
        final Uid winUid = minfo.getWinnerId().get();
        final BidSuccessInGroup winner = uid2Stat.get(winUid);
        final Uid lostUid = minfo.getOpponentUid(winUid).get();
        final BidSuccessInGroup loser = uid2Stat.get(lostUid);

        minfo.getParticipantIdScore().get(winUid)
                .forEach(winner::winBalls);
        minfo.getParticipantIdScore().get(winUid)
                .forEach(loser::lostBalls);
        minfo.getParticipantIdScore().get(lostUid)
                .forEach(loser::winBalls);
        minfo.getParticipantIdScore().get(lostUid)
                .forEach(winner::lostBalls);

        final Map<Uid, Integer> uid2Sets = matchRule.calcWonSets(minfo.getParticipantIdScore());

        loser.wonSets(uid2Sets.get(lostUid));
        loser.lostSets(uid2Sets.get(winUid));
        winner.wonSets(uid2Sets.get(winUid));
        winner.lostSets(uid2Sets.get(lostUid));

        winner.win();
        matchRule.findWinnerId(uid2Sets)
                .ifPresent(uid -> loser.lost()); // walkover = 0
    }

    public GroupPopulations populations(OpenTournamentMemState tournament, int cid) {
        final List<GroupLink> groupLinks = tournament.getGroupsByCategory(cid).stream()
                .sorted(Comparator.comparingInt(GroupInfo::getOrdNumber))
                .map(GroupInfo::toLink)
                .collect(toList());
        final Map<Integer, Long> gidNumMatches = tournament.getParticipants()
                .values().stream()
                .filter(p -> p.getCid() == cid)
                .filter(p -> p.getGid().isPresent())
                .collect(Collectors.groupingBy(p -> p.getGid().get(),
                        Collectors.counting()));
        return GroupPopulations.builder()
                .links(groupLinks)
                .populations(groupLinks.stream()
                        .map(g -> gidNumMatches.getOrDefault(g.getGid(), 0L))
                        .collect(toList()))
                .build();
    }

    public boolean isNotCompleteGroup(OpenTournamentMemState tournament, int gid) {
        final Optional<Integer> ogid = Optional.of(gid);
        return tournament.getMatches()
                .values()
                .stream()
                .anyMatch(m -> ogid.equals(m.getGid())
                        && m.getState() != Over);
    }
}