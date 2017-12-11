package org.dan.ping.pong.app.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.match.Mid;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupMatchResult {
    private ParticipantMatchState state;
    private HisIntPair sets;
    private List<HisIntPair> games;
    private Mid mid;
}
