package org.dan.ping.pong.app.category;

import org.springframework.context.annotation.Import;

@Import({CategoryDaoMysql.class, CategoryService.class, CategoryResource.class})
public class CategoryCtx {
}
