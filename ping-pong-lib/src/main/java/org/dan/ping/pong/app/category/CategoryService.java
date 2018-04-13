package org.dan.ping.pong.app.category;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

@Slf4j
public class CategoryService {
    public int findCidOrCreate(TournamentMemState tournament, int gid,
            TournamentMemState consoleTournament, DbUpdater batch) {
        final int masterCid = tournament.getGroup(gid).getCid();
        final String categoryName = tournament.getCategory(masterCid).getName();
        final Optional<Integer> oCid = consoleTournament.findCidByName(categoryName);
        if (oCid.isPresent()) {
            return oCid.get();
        }
        return createCategory(consoleTournament, categoryName, batch);
    }

    public Set<Integer> findIncompleteCategories(TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getState() != MatchState.Over)
                .map(MatchInfo::getCid)
                .collect(toSet());
    }

    public Stream<MatchInfo> findMatchesInCategoryStream(TournamentMemState tournament, int cid) {
        return tournament.getMatches().values().stream().filter(m -> m.getCid() == cid);
    }

    public CategoryInfo categoryInfo(TournamentMemState tournament,
            int cid, Optional<Uid> ouid) {
        tournament.checkCategory(cid);
        final CategoryLink cLink = tournament.getCategory(cid);
        return CategoryInfo.builder()
                .link(cLink)
                .role(tournament.
                        detectRole(ouid))
                .users(tournament.getParticipants().values().stream()
                        .filter(m -> m.getCid() == cid)
                        .map(ParticipantMemState::toLink)
                        .collect(toList()))
                .build();
    }

    @Inject
    private CategoryDao categoryDao;

    public int createCategory(TournamentMemState tournament, String name, DbUpdater batch) {
        log.info("Add category [{}] to tid {}", name, tournament.getTid());
        final int cid = categoryDao.create(
                NewCategory.builder()
                        .name(name)
                        .tid(tournament.getTid())
                        .build());
        log.info("Category [{}] got id {}", name, cid);
        tournament.getCategories().put(cid, CategoryLink.builder()
                .name(name)
                .cid(cid)
                .build());
        batch.markDirty();
        return cid;
    }
}
