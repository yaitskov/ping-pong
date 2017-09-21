package org.dan.ping.pong.app.playoff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PlayOffRule {
    private int thirdPlaceMatch;
    private int losings;

    public static final PlayOffRule L0_3P = PlayOffRule.builder()
            .losings(0)
            .thirdPlaceMatch(1)
            .build();

    public static final PlayOffRule Losing0 = PlayOffRule.builder()
            .losings(0)
            .thirdPlaceMatch(0)
            .build();
}
