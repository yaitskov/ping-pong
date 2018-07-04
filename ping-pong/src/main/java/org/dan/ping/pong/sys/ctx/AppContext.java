package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.auth.AuthCtx;
import org.dan.ping.pong.app.bid.BidCtx;
import org.dan.ping.pong.app.castinglots.CastingLotsCtx;
import org.dan.ping.pong.app.category.CategoryCtx;
import org.dan.ping.pong.app.city.CityCtx;
import org.dan.ping.pong.app.country.CountryCtx;
import org.dan.ping.pong.app.group.GroupCtx;
import org.dan.ping.pong.app.match.MatchCtx;
import org.dan.ping.pong.app.place.PlaceCtx;
import org.dan.ping.pong.app.sched.ScheduleCtx;
import org.dan.ping.pong.app.score.MatchScoreCtx;
import org.dan.ping.pong.app.sport.SportCtx;
import org.dan.ping.pong.app.suggestion.SuggestionCtx;
import org.dan.ping.pong.app.table.TableCtx;
import org.dan.ping.pong.app.tournament.TournamentCtx;
import org.dan.ping.pong.app.user.UserCtx;
import org.dan.ping.pong.sys.EmailService;
import org.dan.ping.pong.sys.ctx.jackson.JacksonContext;
import org.dan.ping.pong.sys.db.DbContext;
import org.dan.ping.pong.sys.seqex.SeqexCtx;
import org.dan.ping.pong.sys.warmup.WarmUpCtx;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Import({PropertiesContext.class,
        AuthCtx.class,
        UserCtx.class,
        PlaceCtx.class,
        SportCtx.class,
        CategoryCtx.class,
        GroupCtx.class,
        BidCtx.class,
        TableCtx.class,
        ScheduleCtx.class,
        CityCtx.class,
        CountryCtx.class,
        TournamentCtx.class,
        SuggestionCtx.class,
        CastingLotsCtx.class,
        MatchCtx.class,
        MatchScoreCtx.class,
        TimeContext.class,
        JacksonContext.class,
        CronContext.class,
        WarmUpCtx.class,
        EmailService.class,
        SeqexCtx.class,
        DbContext.class})
@EnableTransactionManagement(proxyTargetClass = true)
public class AppContext {
}
