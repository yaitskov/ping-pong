package org.dan.ping.pong.app.group;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.sport.MatchRules;

@Getter
@Setter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DisambiguateMatchRules {
    private boolean onlyForQuitPosition;
    private MatchRules matchRules;
}
