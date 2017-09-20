package org.dan.ping.pong.app.castinglots;

import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.castinglots.rank.ProvidedRankValidator;
import org.springframework.context.annotation.Import;

@Import({CastingLotsDao.class, CastingLotsService.class,
        CastingLotsResource.class, ParticipantRankingService.class,
        ProvidedRankValidator.class})
public class CastingLotsCtx {
}
