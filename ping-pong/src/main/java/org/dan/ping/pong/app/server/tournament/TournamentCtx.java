package org.dan.ping.pong.app.server.tournament;

import org.dan.ping.pong.app.server.tournament.rules.GroupRuleValidator;
import org.dan.ping.pong.app.server.tournament.rules.MatchRuleValidator;
import org.dan.ping.pong.app.server.tournament.rules.TournamentRulesValidator;
import org.springframework.context.annotation.Import;

@Import({TournamentDao.class, TournamentResource.class,
        TournamentCacheFactory.class,
        TournamentCacheLoader.class,
        TournamentCache.class,
        TournamentAccessor.class,
        TournamentRulesValidator.class,
        GroupRuleValidator.class,
        MatchRuleValidator.class,
        TournamentService.class})
public class TournamentCtx {
}
