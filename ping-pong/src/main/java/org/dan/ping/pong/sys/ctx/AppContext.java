package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.server.auth.AuthCtx;
import org.dan.ping.pong.app.server.bid.BidCtx;
import org.dan.ping.pong.app.server.castinglots.CastingLotsCtx;
import org.dan.ping.pong.app.server.category.CategoryCtx;
import org.dan.ping.pong.app.server.city.CityCtx;
import org.dan.ping.pong.app.server.country.CountryCtx;
import org.dan.ping.pong.app.server.group.GroupCtx;
import org.dan.ping.pong.app.server.match.MatchCtx;
import org.dan.ping.pong.app.server.place.PlaceCtx;
import org.dan.ping.pong.app.server.score.MatchScoreCtx;
import org.dan.ping.pong.app.server.table.TableCtx;
import org.dan.ping.pong.app.server.tournament.TournamentCtx;
import org.dan.ping.pong.app.server.user.UserCtx;
import org.dan.ping.pong.sys.EmailService;
import org.dan.ping.pong.sys.ctx.jackson.JacksonContext;
import org.dan.ping.pong.sys.db.DbContext;
import org.dan.ping.pong.sys.seqex.SeqexCtx;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Import({PropertiesContext.class,
        AuthCtx.class,
        UserCtx.class,
        PlaceCtx.class,
        CategoryCtx.class,
        GroupCtx.class,
        BidCtx.class,
        TableCtx.class,
        CityCtx.class,
        CountryCtx.class,
        TournamentCtx.class,
        CastingLotsCtx.class,
        MatchCtx.class,
        MatchScoreCtx.class,
        TimeContext.class,
        JacksonContext.class,
        CronContext.class,
        EmailService.class,
        SeqexCtx.class,
        DbContext.class})
@EnableTransactionManagement(proxyTargetClass = true)
public class AppContext {
}
