package org.dan.ping.pong.app.category;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupRemover;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

@Slf4j
public class CategoryService {
    public static final String NOT_EMPTY_CATEGORY_CANNOT_BE_REMOVED = "Not empty category cannot be removed";

    public Cid findCidOrCreate(TournamentMemState tournament, Gid gid,
            TournamentMemState consoleTournament, DbUpdater batch) {
        final Cid masterCid = tournament.getGroup(gid).getCid();
        final String categoryName = tournament.getCategory(masterCid).getName();
        final Optional<Cid> oCid = consoleTournament.findCidByName(categoryName);
        if (oCid.isPresent()) {
            return oCid.get();
        }
        return createCategory(consoleTournament, categoryName, batch);
    }

    public Set<Cid> findIncompleteCategories(TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getState() != MatchState.Over)
                .map(MatchInfo::getCid)
                .collect(toSet());
    }

    public Stream<MatchInfo> findMatchesInCategoryStream(TournamentMemState tournament, Cid cid) {
        return tournament.getMatches().values().stream().filter(m -> m.getCid().equals(cid));
    }

    public CategoryInfo categoryInfo(TournamentMemState tournament,
            Cid cid, Optional<Uid> ouid) {
        tournament.checkCategory(cid);
        final CategoryLink cLink = tournament.getCategory(cid);
        return CategoryInfo.builder()
                .link(cLink)
                .role(tournament.
                        detectRole(ouid))
                .users(tournament.findBidsByCategory(cid)
                        .map(ParticipantMemState::toBidLink)
                        .collect(toList()))
                .build();
    }

    @Inject
    private CategoryDao categoryDao;

    public Cid createCategory(TournamentMemState tournament, String name, DbUpdater batch) {
        final Cid cid = tournament.getNextCategory().next();
        log.info("Add category [{}/{}] to tid {}", cid, name, tournament.getTid());
        categoryDao.create(
                NewCategory.builder()
                        .name(name)
                        .tid(tournament.getTid())
                        .cid(cid)
                        .build(),
                batch);
        tournament.getCategories().put(cid, CategoryLink.builder()
                .name(name)
                .cid(cid)
                .build());
        return cid;
    }

    @Inject
    private GroupRemover gidRemover;

    public void removeCategory(TournamentMemState tournament, Cid cid, DbUpdater batch) {
        log.info("Remove category {}/{}", cid, tournament.getCategory(cid).getName());
        if (tournament.findBidsByCategory(cid).count() > 0) {
            throw badRequest(NOT_EMPTY_CATEGORY_CANNOT_BE_REMOVED);
        }
        gidRemover.removeByCategory(tournament, cid, batch);
        categoryDao.delete(tournament.getTid(), cid, batch);
        tournament.getCategories().remove(cid);
    }
}
