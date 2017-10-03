package org.dan.ping.pong.app.category;

import org.springframework.context.annotation.Import;

@Import({CategoryDao.class, CategoryService.class, CategoryResource.class})
public class CategoryCtx {
}
