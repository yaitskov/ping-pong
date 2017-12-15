package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardRules {
    public static final RewardRules defaultRewards = RewardRules.builder()
            .bestScore(100)
            .nextScoreStep(3)
            .build();

    private int bestScore;
    private int nextScoreStep;
}
