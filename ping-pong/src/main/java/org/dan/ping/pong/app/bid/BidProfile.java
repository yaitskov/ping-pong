package org.dan.ping.pong.app.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.category.CategoryInfo;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidProfile {
    private BidState state;
    private Instant enlistedAt;
    private String name;
    private CategoryInfo category;
}
