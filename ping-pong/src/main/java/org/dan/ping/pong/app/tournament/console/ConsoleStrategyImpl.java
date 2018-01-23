package org.dan.ping.pong.app.tournament.console;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Set;

import javax.inject.Inject;

public class ConsoleStrategyImpl implements ConsoleStrategy {

    @Inject
    private TournamentService tournamentService;

    @Inject
    private CategoryService categoryService;

    @Override
    public void onGroupComplete(int gid, TournamentMemState tournament, Set<Uid> quitUids, DbUpdater batch) {
        createConsoleTournamentIfNotExists(tournament);
        /*
        create console tournament if not exists
        copy-create category if not exists (how to keep association between categories parent id ref? by name?)
         enlist losers to it
         if all groups in the category  are complete - start console tournament
         controversy - other categories (groups) could not be ready.

         the problem is the same for normal tournament with multiple categories.

         handle the situation when 1 category is complete while second is not started jet.

         tournament status partially loses its purpose in favor of introduced category status (
                    when add participants, start console tournament)

         tournament state: Hidden, Announce, Draft, Open, Close, Canceled, Replaced

         category status affects tournament status
            category state: Open, Complete

            if cat state : Complete -> Open       tournament state:  Close -> Open

            tournament state:  Open -> Close  if all cat state Complete |

         */
    }

    private void createConsoleTournamentIfNotExists(TournamentMemState tournament) {

    }
}
