package org.dan.ping.pong.app.castinglots;

import static java.util.Optional.ofNullable;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class GeneratingConsoleLayersForBaseMatches extends LadderSeedingAlg<Integer> {
    private final MergedGrpLrdPlayOffBuilder mergedGrpLrdPlayOffBuilder;
    private final Map<Integer, List<ParticipantMemState>> bidsByLevels;

    @Override
    protected void seedInMatch(SelectedCid sCid, Mid rootMid, Optional<Integer> levelO) {
        mergedGrpLrdPlayOffBuilder.buildLayer(
                levelO.flatMap(l -> ofNullable(bidsByLevels.get(l)))
                        .orElseGet(Collections::emptyList),
                rootMid,
                levelO,
                sCid);
    }
}
