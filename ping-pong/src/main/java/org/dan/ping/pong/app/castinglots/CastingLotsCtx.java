package org.dan.ping.pong.app.castinglots;

import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRuleValidator;
import org.springframework.context.annotation.Import;

@Import({CastingLotsDao.class, CastingLotsService.class,
        CastingLotsResource.class, ParticipantRankingService.class,
        CastingLotsRuleValidator.class, GroupDivider.class})
public class CastingLotsCtx {
}
