package org.dan.ping.pong.app.server.tournament;


import org.dan.ping.pong.app.server.bid.BidCtx;
import org.dan.ping.pong.app.server.castinglots.CastingLotsCtx;
import org.dan.ping.pong.app.server.category.CategoryCtx;
import org.dan.ping.pong.app.server.group.GroupCtx;
import org.dan.ping.pong.app.server.match.ForTestBidDao;
import org.dan.ping.pong.app.server.match.ForTestMatchDao;
import org.dan.ping.pong.app.server.match.MatchCtx;
import org.dan.ping.pong.app.server.place.PlaceCtx;
import org.dan.ping.pong.app.server.score.MatchScoreCtx;
import org.dan.ping.pong.app.server.score.MatchScoreDao;
import org.dan.ping.pong.app.server.table.TableCtx;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.sys.ctx.BaseTestContext;
import org.springframework.context.annotation.Import;

@Import({BaseTestContext.class, PlaceCtx.class, CategoryCtx.class,
        TournamentCtx.class, BidCtx.class, CastingLotsCtx.class,
        MatchCtx.class, MatchScoreCtx.class, TableCtx.class,
        GroupCtx.class, MatchScoreDao.class, ForTestMatchDao.class,
        ForTestBidDao.class, Simulator.class})
public class JerseyWithSimulator {}
