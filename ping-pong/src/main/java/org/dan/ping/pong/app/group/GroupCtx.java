package org.dan.ping.pong.app.group;

import org.springframework.context.annotation.Import;

@Import({GroupDaoImpl.class, GroupService.class,
        GroupRemover.class,
        GroupResource.class})
public class GroupCtx {
}
