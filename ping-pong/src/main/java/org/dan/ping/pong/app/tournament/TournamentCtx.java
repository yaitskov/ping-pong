package org.dan.ping.pong.app.tournament;

import org.springframework.context.annotation.Import;

@Import({TournamentDao.class, TournamentResource.class,
        TournamentCacheFactory.class,
        TournamentCache.class,
        TournamentAccessor.class,
        TournamentService.class})
public class TournamentCtx {
}
