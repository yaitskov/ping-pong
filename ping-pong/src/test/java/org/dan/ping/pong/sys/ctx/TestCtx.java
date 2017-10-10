package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.bid.BidCtx;
import org.dan.ping.pong.app.castinglots.CastingLotsCtx;
import org.dan.ping.pong.app.category.CategoryCtx;
import org.dan.ping.pong.app.group.GroupCtx;
import org.dan.ping.pong.app.match.MatchCtx;
import org.dan.ping.pong.app.place.PlaceCtx;
import org.dan.ping.pong.app.sched.ScheduleCtx;
import org.dan.ping.pong.app.score.MatchScoreCtx;
import org.dan.ping.pong.app.table.TableCtx;
import org.dan.ping.pong.app.tournament.TournamentCtx;
import org.springframework.context.annotation.Import;

@Import({BaseTestContext.class, PlaceCtx.class, CategoryCtx.class,
        TournamentCtx.class, BidCtx.class, CastingLotsCtx.class,
        MatchCtx.class, MatchScoreCtx.class, TableCtx.class, ScheduleCtx.class,
        GroupCtx.class})
public class TestCtx {
}
