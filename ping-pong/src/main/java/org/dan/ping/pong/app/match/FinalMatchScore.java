package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalMatchScore {
    private int tid;
    private int mid;
    private List<IdentifiedScore> scores;
}
