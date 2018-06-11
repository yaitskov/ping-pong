package org.dan.ping.pong.sys.warmup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class WarmUpRequest {
    @NotBlank
    private String action;
    @NotNull
    private Instant clientTime;
}
