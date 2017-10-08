package org.dan.ping.pong.app.group;

import org.springframework.context.annotation.Import;

@Import({GroupDao.class, GroupService.class, GroupResource.class})
public class GroupCtx {
}
