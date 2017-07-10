package org.dan.ping.pong.app.tournament;

import org.springframework.context.annotation.Import;

@Import({TournamentDao.class, TournamentResource.class,
        TournamentService.class})
public class TournamentCtx {
}
