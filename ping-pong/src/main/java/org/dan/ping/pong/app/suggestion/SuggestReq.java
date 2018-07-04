package org.dan.ping.pong.app.suggestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestReq {
    @NotNull
    @NotEmpty
    private String pattern;
    @Valid
    @NotNull
    private PageAdr page;
}
