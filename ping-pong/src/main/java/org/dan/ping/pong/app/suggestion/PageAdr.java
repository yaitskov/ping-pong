package org.dan.ping.pong.app.suggestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageAdr {
    @Min(0)
    private int page;
    @Min(1)
    @Max(1000)
    private int size;

    public static PageAdr ofSize(int size) {
        return PageAdr
                .builder()
                .page(0)
                .size(size)
                .build();
    }

    public int total() {
        return (page + 1) * size;
    }

    public int offset() {
        return page * size;
    }
}
