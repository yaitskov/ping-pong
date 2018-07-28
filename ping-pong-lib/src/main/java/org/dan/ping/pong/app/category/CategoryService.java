package org.dan.ping.pong.app.category;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.category.CategoryState.End;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupRemover;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.console.ConsoleStrategy;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

@Slf4j
public class CategoryService {
    public static final String NOT_EMPTY_CATEGORY_CANNOT_BE_REMOVED
            = "Not empty category cannot be removed";

    public Cid findCidOrCreate(TournamentMemState tournament, Gid gid,
            TournamentMemState consoleTournament, DbUpdater batch) {
        final Cid masterCid = tournament.getGroup(gid).getCid();
        return findCidOrCreate(tournament, masterCid, consoleTournament, batch);
    }

    public Cid findCidOrCreate(TournamentMemState tournament, Cid masterCid,
            TournamentMemState consoleTournament, DbUpdater batch) {
        final String categoryName = tournament.getCategory(masterCid).getName();
        final Optional<Cid> oCid = consoleTournament.findCidByNameO(categoryName);
        if (oCid.isPresent()) {
            return oCid.get();
        }
        return createCategory(consoleTournament, categoryName, batch);
    }

    public List<Cid> findIncompleteCategories(TournamentMemState tournament) {
        return tournament.getCategories().values().stream()
                .filter(c -> c.getState() != End)
                .map(CategoryMemState::getCid)
                .collect(toList());
    }

    public Stream<MatchInfo> findMatchesInCategoryStream(
            TournamentMemState tournament, Cid cid) {
        return tournament.getMatches().values()
                .stream().filter(m -> m.getCid().equals(cid));
    }

    public CategoryInfo categoryInfo(TournamentMemState tournament,
            Cid cid, Optional<Uid> ouid) {
        tournament.checkCategory(cid);
        final CategoryMemState cLink = tournament.getCategory(cid);
        return CategoryInfo.builder()
                .link(cLink.toLink())
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
        tournament.getCategories().put(cid, CategoryMemState.builder()
                .name(name)
                .state(CategoryState.Drt)
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

    public static Map<Cid, List<ParticipantMemState>> groupByCategories(
            List<ParticipantMemState> bids) {
        return bids.stream().collect(groupingBy(
                ParticipantMemState::getCid, toList()));
    }

    public void setState(Tid tid, CategoryMemState catSt, CategoryState targetSt, DbUpdater batch) {
        final CategoryState oldSt = catSt.getState();
        if (oldSt != targetSt) {
            catSt.setState(targetSt);
            categoryDao.setState(tid, catSt.getCid(), oldSt,  targetSt, batch);
        }
    }

    @Inject
    private ConsoleStrategy consoleStrategy;

    public void markComplete(TournamentMemState tournament, CategoryMemState cat, DbUpdater batch) {
        setState(tournament.getTid(), cat, End, batch);
        consoleStrategy.onPlayOffCategoryComplete(cat.getCid(), tournament, batch);

    }
}
