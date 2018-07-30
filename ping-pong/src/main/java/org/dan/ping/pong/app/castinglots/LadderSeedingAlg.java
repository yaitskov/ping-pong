package org.dan.ping.pong.app.castinglots;

import static java.lang.Math.max;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.PLAY_OFF_SEEDS;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;

import java.util.List;
import java.util.Optional;

public abstract class LadderSeedingAlg<C> {
    public void seedBaseMatches(int basePositions,
            List<MatchInfo> sortedBaseMatches,
            List<C> orderedChildren,
            SelectedCid sCid) {
        final List<Integer> seeds = ofNullable(PLAY_OFF_SEEDS.get(basePositions))
                .orElseThrow(() -> internalError("No seeding for "
                        + orderedChildren.size() + " participants"));

        for (int iMatch = 0; iMatch < basePositions / 2; ++iMatch) {
            final MatchInfo match = sortedBaseMatches.get(iMatch);
            final int iChild1 = seeds.get(iMatch * 2);
            final int iChild2 = seeds.get(iMatch * 2 + 1);
            final int iStrongChild = Math.min(iChild1, iChild2);
            final int iWeakChild = max(iChild1, iChild2);

            seedInMatch(sCid, match.getMid(),
                    Optional.of(orderedChildren.get(iStrongChild)));

            seedInMatch(sCid, match.getMid(),
                    iWeakChild >= orderedChildren.size()
                            ? Optional.empty()
                            : Optional.of(orderedChildren.get(iWeakChild)));
        }
    }

    protected abstract void seedInMatch(SelectedCid sCid, Mid mid, Optional<C> child);
}
