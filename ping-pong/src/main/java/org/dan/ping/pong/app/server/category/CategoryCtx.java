package org.dan.ping.pong.app.server.category;

import org.springframework.context.annotation.Import;

@Import({CategoryDao.class, CategoryService.class, CategoryResource.class})
public class CategoryCtx {
}
