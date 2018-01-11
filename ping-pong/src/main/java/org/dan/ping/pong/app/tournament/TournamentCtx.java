package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.app.tournament.rules.GroupRuleValidator;
import org.dan.ping.pong.app.tournament.rules.PingPongMatchRuleValidator;
import org.dan.ping.pong.app.tournament.rules.TournamentRulesValidator;
import org.springframework.context.annotation.Import;

@Import({TournamentDao.class, TournamentResource.class,
        TournamentCacheFactory.class,
        TournamentCacheLoader.class,
        TournamentCache.class,
        TournamentAccessor.class,
        TournamentRulesValidator.class,
        GroupRuleValidator.class,
        PingPongMatchRuleValidator.class,
        TournamentService.class})
public class TournamentCtx {
}
