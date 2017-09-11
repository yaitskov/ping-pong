package org.dan.ping.pong.mock.simulator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.primitives.Ints.asList;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class FixedSetGenerator implements SetGenerator {
    private int setNumber;
    @Getter
    private final Player playerA;
    @Getter
    private final Player playerB;
    private final List<Integer> games;

    public static FixedSetGenerator game(Player a, Player b, int... games) {
        checkArgument(games.length % 2 == 0);
        return new FixedSetGenerator(a, b, asList(games));
    }

    @Override
    @SneakyThrows
    public Map<Player, Integer> generate() {
        final int gamesA = games.get(setNumber * 2);
        final int gamesB = games.get(setNumber * 2 + 1);
        ++setNumber;
        if (gamesB == 0 && gamesA == -1) {
            return ImmutableMap.of(playerA, -1);
        } else if (gamesB == -1 && gamesA == 0) {
            return ImmutableMap.of(playerB, -1);
        } else if (gamesB == 0 && gamesA == 0) {
            log.info("Pause scoring set {}. Press Enter to continue", setNumber);
            System.in.read();
            return generate();
        }
        final Map<Player, Integer> result = ImmutableMap.of(playerA, gamesA, playerB, gamesB);
        return result;
    }

    @Override
    public boolean isEmpty() {
        return setNumber == games.size() / 2;
    }

    @Override
    public int getSetNumber() {
        return setNumber;
    }
}
