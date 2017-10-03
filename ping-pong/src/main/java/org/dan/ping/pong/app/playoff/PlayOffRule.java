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

    public static final PlayOffRule L1_3P = PlayOffRule.builder()
            .losings(1)
            .thirdPlaceMatch(1)
            .build();

    public static final PlayOffRule Losing1 = PlayOffRule.builder()
            .losings(1)
            .thirdPlaceMatch(0)
            .build();

    public static final PlayOffRule Losing2 = PlayOffRule.builder()
            .losings(2)
            .thirdPlaceMatch(1) // has no sense
            .build();
}
