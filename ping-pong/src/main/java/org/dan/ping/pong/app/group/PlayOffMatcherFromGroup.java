package org.dan.ping.pong.app.group;

import static com.google.common.primitives.Ints.asList;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

public class PlayOffMatcherFromGroup {
    private static final Map<Integer, Map<Integer, Map<Integer, List<Integer>>>> MAP =
            ImmutableMap.of( // groups
                    1, ImmutableMap.of( // quits group
                            2, ImmutableMap.of(0, // group
                                    asList(0, 0)),
                            4, ImmutableMap.of(0, // group
                                    asList(0, 1, 0, 1))),
                    2, ImmutableMap.of( // quits group
                            1, ImmutableMap.of( // group
                                    0, asList(0),
                                    1, asList(0)),
                            2, ImmutableMap.of( // group
                                    0, asList(0, 1),
                                    1, asList(1, 0))),
                    4, ImmutableMap.of( // quits group
                            1, ImmutableMap.of( // group
                                    0, asList(0),
                                    1, asList(0),
                                    2, asList(1),
                                    3, asList(1)),
                            2, ImmutableMap.of( // group
                                    0, asList(0, 3),
                                    1, asList(3, 0),
                                    2, asList(2, 1),
                                    3, asList(1, 2))));

    public static List<Integer> find(int quits, int groupOrdNumber, int groups) {
        return ofNullable(MAP.get(groups))
                .map(m -> ofNullable(m.get(quits))
                        .map(m2 -> ofNullable(m2.get(groupOrdNumber))
                                .orElseThrow(() -> internalError("No mapping for "
                                        + groups + ":" + quits
                                        + ":" + groupOrdNumber
                                        + " groups:quits:groupNumber")))
                        .orElseThrow(() -> internalError("No mapping for "
                                + groups + ":" + quits + " groups:quits"))
                )
                .orElseThrow(() -> internalError("No mapping for " + groups + " groups"));
    }
}
