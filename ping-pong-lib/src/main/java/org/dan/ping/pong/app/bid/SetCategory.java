package org.dan.ping.pong.app.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetCategory {
    private Tid tid;
    private Bid bid;
    private Cid expectedCid;
    private Cid targetCid;
    private Optional<Gid> targetGid = Optional.empty();

    public static class SetCategoryBuilder {
        Optional<Gid> targetGid = Optional.empty();
    }
}
