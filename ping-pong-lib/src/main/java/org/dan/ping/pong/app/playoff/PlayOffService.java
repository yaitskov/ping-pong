package org.dan.ping.pong.app.playoff;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.MID0;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.tournament.CumulativeScore.createComparator;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchValidationRule;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.tournament.CumulativeScore;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

public class PlayOffService {
    public List<MatchInfo> findBaseMatches(TournamentMemState tournament, int cid) {
        return findBaseMatches(findPlayOffMatches(tournament, cid));
    }

    public List<MatchInfo> findBaseMatches(List<MatchInfo> cidMatches) {
        Map<Mid, Mid> midChild = new HashMap<>();
        cidMatches.forEach(m -> {
            m.getWinnerMid().ifPresent(wmid -> midChild.put(wmid, m.getMid()));
            m.getLoserMid().ifPresent(wmid -> midChild.put(wmid, m.getMid()));
        });
        return cidMatches.stream()
                .filter(m -> !m.getGid().isPresent())
                .filter(m -> !midChild.containsKey(m.getMid()))
                .collect(toList());
    }

    public Collection<MatchInfo> findNextMatches(Map<Mid, MatchInfo> matches,
            Collection<MatchInfo> baseMatches) {
        return baseMatches.stream()
                .flatMap(m -> Stream.of(
                        ofNullable(matches.get(m.getWinnerMid().orElse(MID0))),
                        ofNullable(matches.get(m.getLoserMid().orElse(MID0))))
                        .filter(Optional::isPresent)
                        .map(Optional::get))
                .collect(toMap(MatchInfo::getMid, o -> o, (a, b) -> a))
                .values();
    }

    public Collection<MatchInfo> findCompleteGroupMatches(List<MatchInfo> matches) {
        return matches.stream().filter(m -> m.getGid().isPresent())
                .filter(m -> m.getState() == Over)
                .collect(toList());
    }

    public List<MatchInfo> findPlayOffMatches(TournamentMemState tournament, int cid) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getCid() == cid)
                .filter(minfo -> !minfo.getGid().isPresent())
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .collect(toList());
    }

    @Inject
    private GroupService groupService;

    @Inject
    private CategoryService categoryService;

    public List<TournamentResultEntry> playOffResult(TournamentMemState tournament, int cid,
            List<TournamentResultEntry> groupOrdered) {
        final List<MatchInfo> cidMatches = categoryService.findMatchesInCategory(tournament, cid);

        final List<MatchInfo> cidPlayOffMatches = cidMatches
                .stream()
                .filter(minfo -> !minfo.getGid().isPresent())
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .collect(toList());

        final Set<Uid> playOffUids = participantsOf(cidPlayOffMatches);
        Map<Uid, Integer> quitUid2Position = extractQuittingUids(playOffUids, groupOrdered);

        int level = quitUid2Position.values().stream()
                .map(n -> (int) Math.round(Math.ceil(Math.log10(n + 1))))
                .max(Integer::compare)
                .orElse(0);
        final Map<Uid, CumulativeScore> uidLevel = new HashMap<>();
        Collection<MatchInfo> baseMatches = findBaseMatches(cidPlayOffMatches);
        final MatchValidationRule matchRules = tournament.getRule().getMatch();
        while (true) {
            groupService.ranksLevelMatches(tournament, level++, uidLevel, baseMatches, matchRules);
            final Collection<MatchInfo> nextLevel = findNextMatches(tournament.getMatches(), baseMatches);
            if (nextLevel.isEmpty()) {
                break;
            }
            baseMatches = nextLevel;
        }
        quitUid2Position.forEach((uid, positionInGroup) -> {
            uidLevel.get(uid).getWeighted().lostBalls(positionInGroup);
        });
        return uidLevel.values().stream()
                .sorted(createComparator(tournament.disambiguationPolicy().getComparator()))
                .map(cuScore -> {
                    final ParticipantMemState participant = tournament.getParticipant(
                            cuScore.getRating().getUid());
                    return TournamentResultEntry.builder()
                            .user(participant.toLink())
                            .state(participant.getState())
                            .punkts(cuScore.getRating().getPunkts())
                            .score(cuScore)
                            .build();
                })
                .collect(toList());
    }

    public Map<Uid, Integer> extractQuittingUids(Set<Uid> playOffUids, List<TournamentResultEntry> groupOrdered) {
        final Map<Uid, Integer> result = new HashMap<>();
        for (int i = groupOrdered.size() - 1; i >= 0; --i) {
            TournamentResultEntry entry = groupOrdered.get(i);
            if (playOffUids.contains(entry.getUser().getUid())) {
                result.put(entry.getUser().getUid(),  i);
                groupOrdered.remove(i);
            }
        }
        return result;
    }

    public Set<Uid> participantsOf(List<MatchInfo> matches) {
        return matches.stream().flatMap(MatchInfo::participants).collect(Collectors.toSet());
    }
}
