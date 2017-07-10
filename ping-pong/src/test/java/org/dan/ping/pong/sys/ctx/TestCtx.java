package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.bid.BidCtx;
import org.dan.ping.pong.app.castinglots.CastingLotsCtx;
import org.dan.ping.pong.app.group.GroupCtx;
import org.dan.ping.pong.app.match.MatchCtx;
import org.dan.ping.pong.app.score.MatchScoreCtx;
import org.dan.ping.pong.app.table.TableCtx;
import org.dan.ping.pong.app.tournament.TournamentCtx;
import org.springframework.context.annotation.Import;

@Import({BaseTestContext.class,
        TournamentCtx.class, BidCtx.class,CastingLotsCtx.class,
        MatchCtx.class, MatchScoreCtx.class, TableCtx.class,
        GroupCtx.class})
public class TestCtx {
}
