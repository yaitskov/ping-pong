package org.dan.ping.pong.app.match;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.sys.error.Error;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class EffectHashMismatchError extends Error {
    private final String error = "effectHashMismatch";
    private Optional<String> effectHash;
    private List<EffectedMatch> matchesToBeReset;
    private List<NewEffectedMatch> matchesToBeCreated;
    private List<EffectedMatch> matchesToBeRemoved;

    public EffectHashMismatchError(Optional<String> effectHash,
            List<EffectedMatch> toBeReset,
            List<NewEffectedMatch> toBeCreated,
            List<EffectedMatch> toBeRemoved) {
        super("Effect hash is mismatch");
        this.effectHash = effectHash;
        this.matchesToBeReset = toBeReset;
        this.matchesToBeCreated = toBeCreated;
        this.matchesToBeRemoved = toBeRemoved;
    }
}
