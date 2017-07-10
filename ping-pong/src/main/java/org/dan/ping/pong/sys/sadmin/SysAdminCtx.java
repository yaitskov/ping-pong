package org.dan.ping.pong.sys.sadmin;

import org.springframework.context.annotation.Import;

@Import({ShaProvider.class, PasswordHasher.class, SysAdminDao.class})
public class SysAdminCtx {}
