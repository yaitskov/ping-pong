package org.dan.ping.pong.app.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMemState {
    private Cid cid;
    private String name;
    private CategoryState state;

    public CategoryLink toLink() {
        return CategoryLink.builder()
                .cid(cid)
                .name(name)
                .build();
    }
}
