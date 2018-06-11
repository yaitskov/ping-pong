package org.dan.ping.pong.sys.warmup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class WarmUpRequest {
    private String action;
    private Instant clientTime;
}
