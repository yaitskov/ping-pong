package org.dan.ping.pong.app.server.castinglots;

import org.dan.ping.pong.app.server.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.server.castinglots.rank.CastingLotsRuleValidator;
import org.dan.ping.pong.app.server.group.GroupSizeCalculator;
import org.springframework.context.annotation.Import;

@Import({CastingLotsDao.class, CastingLotsService.class,
        CastingLotsResource.class, ParticipantRankingService.class,
        CastingLotsRuleValidator.class, GroupDivider.class, GroupSizeCalculator.class})
public class CastingLotsCtx {
}
