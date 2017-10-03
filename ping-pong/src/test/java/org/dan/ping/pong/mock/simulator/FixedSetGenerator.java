package org.dan.ping.pong.mock.simulator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.primitives.Ints.asList;
import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.match.MatchResource;
import org.dan.ping.pong.app.match.ResetSetScore;
import org.dan.ping.pong.mock.MyRest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class FixedSetGenerator implements SetGenerator {
    @Setter
    private int mid;
    private int setNumber;
    private int index;
    @Getter
    private final Player playerA;
    @Getter
    private final Player playerB;
    private final List<Integer> games;
    private final Map<Integer, BiConsumer<TournamentScenario, Integer>> actionsBefore = new HashMap<>();

    public static FixedSetGenerator game(Player a, Player b, int... games) {
        checkArgument(games.length % 2 == 0);
        return new FixedSetGenerator(a, b, new ArrayList<>(asList(games)));
    }

    public FixedSetGenerator exec(BiConsumer<TournamentScenario, Integer> action) {
        checkArgument(actionsBefore.putIfAbsent(games.size() / 2, action) == null,
                "Action override at set %d", games.size());
        return this;
    }

    public FixedSetGenerator set(int a, int b) {
        games.add(a);
        games.add(b);
        return this;
    }

    public FixedSetGenerator pause() {
        return exec((scenario, mid) -> {
            log.info("Pause scoring set {}. Press Enter to continue", setNumber);
            try {
                System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public FixedSetGenerator reset(int targetSetNumber, MyRest myRest) {
        return exec((scenario, mid) -> {
            myRest.voidPost(MatchResource.MATCH_RESET_SET_SCORE,
                    scenario.getTestAdmin(),
                    ResetSetScore.builder().tid(scenario.getTid())
                            .mid(mid).setNumber(targetSetNumber)
                            .build());
            setNumber = targetSetNumber;
        });
    }

    @Override
    @SneakyThrows
    public Map<Player, Integer> generate(TournamentScenario scenario) {
        final int gamesA = games.get(index * 2);
        final int gamesB = games.get(index * 2 + 1);
        ofNullable(actionsBefore.get(index)).ifPresent(r -> r.accept(scenario, mid));
        ++setNumber;
        ++index;
        if (gamesB == 0 && gamesA == -1) {
            return ImmutableMap.of(playerA, -1);
        } else if (gamesB == -1 && gamesA == 0) {
            return ImmutableMap.of(playerB, -1);
        } else if (gamesB == 0 && gamesA == 0) {
            log.info("Pause scoring set {}. Press Enter to continue", setNumber);
            System.in.read();
            return generate(scenario);
        }
        final Map<Player, Integer> result = ImmutableMap.of(playerA, gamesA, playerB, gamesB);
        return result;
    }

    @Override
    public boolean isEmpty() {
        return index == games.size() / 2;
    }

    @Override
    public int getSetNumber() {
        return setNumber;
    }
}
