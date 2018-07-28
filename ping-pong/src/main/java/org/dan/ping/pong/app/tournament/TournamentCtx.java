package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.app.tournament.console.ConsoleCtx;
import org.dan.ping.pong.app.tournament.console.ConsoleTournamentService;
import org.dan.ping.pong.app.tournament.console.TournamentRelationCacheFactory;
import org.dan.ping.pong.app.tournament.console.TournamentRelationCacheLoader;
import org.dan.ping.pong.app.tournament.marshaling.TournamentMarshalingCtx;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;
import org.dan.ping.pong.app.tournament.rules.GroupRuleValidator;
import org.dan.ping.pong.app.tournament.rules.PingPongMatchRuleValidator;
import org.dan.ping.pong.app.tournament.rules.TournamentRulesValidator;
import org.springframework.context.annotation.Import;

@Import({TournamentDaoMySql.class, TournamentResource.class,
        TournamentRelationCacheFactory.class,
        TournamentRelationCacheLoader.class,
        TournamentCacheFactory.class,
        TournamentCacheLoader.class,
        TournamentCache.class,
        ConsoleCtx.class,
        TournamentAccessor.class,
        TournamentRulesValidator.class,
        GroupRuleValidator.class,
        PingPongMatchRuleValidator.class,
        TournamentTerminator.class,
        ChildTournamentProvider.class,
        TournamentMarshalingCtx.class,
        ConsoleTournamentService.class,
        RelatedTournamentsService.class,
        TournamentService.class})
public class TournamentCtx {
}
