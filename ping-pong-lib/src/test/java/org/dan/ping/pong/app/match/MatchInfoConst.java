package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.group.GroupService.MATCH_TAG_DISAMBIGUATION;

import java.util.Optional;

public class MatchInfoConst {
    public static final MatchInfo GROUP_ORIGIN_MATCH = MatchInfo
            .builder()
            .mid(new Mid(1))
            .cid(2)
            .gid(Optional.of(3))
            .tag(Optional.empty())
            .build();

    public static final MatchInfo GROUP_DM_MATCH = MatchInfo
            .builder()
            .mid(new Mid(1))
            .cid(2)
            .gid(Optional.of(3))
            .tag(MATCH_TAG_DISAMBIGUATION)
            .build();

    public static final MatchInfo PLAY_OFF_MATCH_HF = MatchInfo
            .builder()
            .mid(new Mid(1))
            .cid(2)
            .level(1)
            .gid(Optional.empty())
            .tag(Optional.empty())
            .build();
}
