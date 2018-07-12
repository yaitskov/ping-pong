package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.group.GroupService.MATCH_TAG_DISAMBIGUATION;

import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;

import java.util.Optional;

public class MatchInfoConst {
    public static final Cid CID = Cid.of(2);
    public static final Optional<Gid> GID = Optional.of(Gid.of(3));
    public static final MatchInfo GROUP_ORIGIN_MATCH = MatchInfo
            .builder()
            .mid(new Mid(1))
            .cid(CID)
            .gid(GID)
            .tag(Optional.empty())
            .build();

    public static final MatchInfo GROUP_DM_MATCH = MatchInfo
            .builder()
            .mid(new Mid(1))
            .cid(CID)
            .gid(GID)
            .tag(MATCH_TAG_DISAMBIGUATION)
            .build();

    public static final MatchInfo PLAY_OFF_MATCH_HF = MatchInfo
            .builder()
            .mid(new Mid(1))
            .cid(CID)
            .level(1)
            .gid(Optional.empty())
            .tag(Optional.empty())
            .build();
}
