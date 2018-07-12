package org.dan.ping.pong.app.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChangeGroupReq {
    private Tid tid;
    private Bid bid;
    private Gid expectedGid;
    private Optional<Gid> targetGid;
}
