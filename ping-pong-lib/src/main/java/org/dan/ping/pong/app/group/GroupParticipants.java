package org.dan.ping.pong.app.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.Collection;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupParticipants {
    private Collection<GroupParticipantResult> participants;
    private Tid tid;
    private int quitsGroup;
}
