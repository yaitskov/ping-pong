package org.dan.ping.pong.app.match;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.sys.error.Error;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EffectHashMismatchError extends Error {
    private final String error = "effectHashMismatch";
    private String effectHash;
    private List<EffectedMatch> effectedMatches;

    public EffectHashMismatchError(String effectHash, List<EffectedMatch> effectedMatches) {
        super("Effect hash is mismatch");
        this.effectHash = effectHash;
        this.effectedMatches = effectedMatches;
    }
}
