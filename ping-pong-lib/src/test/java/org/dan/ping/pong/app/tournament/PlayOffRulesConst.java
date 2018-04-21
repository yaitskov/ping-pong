package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.match.MatchRulesConst.S3A2G11;

import org.dan.ping.pong.app.playoff.PlayOffRule;

import java.util.Optional;

public class PlayOffRulesConst {
    public static final PlayOffRule L1_S3A2G11 = PlayOffRule.builder()
            .losings(1)
            .thirdPlaceMatch(0)
            .match(Optional.of(S3A2G11))
            .build();
}
