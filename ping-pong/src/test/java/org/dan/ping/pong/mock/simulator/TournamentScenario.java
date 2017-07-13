package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.mock.simulator.GameOutcome.L03;
import static org.dan.ping.pong.mock.simulator.GameOutcome.L13;
import static org.dan.ping.pong.mock.simulator.GameOutcome.L23;
import static org.dan.ping.pong.mock.simulator.GameOutcome.W30;
import static org.dan.ping.pong.mock.simulator.GameOutcome.W31;
import static org.dan.ping.pong.mock.simulator.GameOutcome.W32;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.mock.TestUserSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@ToString(of = {"uidPlayer", "categoryDbId", "tid", "placeId", "params"})
public class TournamentScenario {
    private Optional<String> name = empty();
    private final Map<Set<Player>, GameEnd> groupMatches = new HashMap<>();
    private final Map<Set<Player>, GameEnd> playOffMatches = new HashMap<>();
    private final Multimap<PlayerCategory, Player> playersByCategories = HashMultimap.create();
    private final Map<Player, PlayerCategory> playersCategory = new HashMap<>();
    private final Set<Player> playOffPlayers = new HashSet<>();
    private final List<Player> champions = new ArrayList<>();
    private final Map<Player, TestUserSession> playersSessions = new HashMap<>();
    private final Map<Integer, Player> uidPlayer = new HashMap<>();
    private final Map<PlayerCategory, Integer> categoryDbId = new HashMap<>();
    private final Map<Set<Player>, Pause> pauseOnMatches = new HashMap<>();
    @Setter
    private int tid;
    @Setter
    private int placeId;
    @Setter
    private SimulatorParams params;

    public static TournamentScenario begin() {
        return new TournamentScenario();
    }

    public TournamentScenario name(String namePrefix) {
        this.name = Optional.of(namePrefix);
        return this;
    }

    private TournamentScenario match(Player pa, GameOutcome outcome, Player pb) {
        playersSessions.put(pa, null);
        playersSessions.put(pb, null);
        GameEnd match = GameEnd.game(pa, outcome, pb);
        if (!playOffPlayers.contains(pa) && !playOffPlayers.contains(pb)) {
            if (groupMatches.putIfAbsent(new HashSet<>(match.getParticipants()), match) != null) {
                throw new IllegalStateException("Multiple matches between " +
                        "the same participants are not supported");
            }
        } else if (playOffPlayers.contains(pa) && playOffPlayers.contains(pb)) {
             if (playOffMatches.putIfAbsent(new HashSet<>(match.getParticipants()), match) != null) {
                 throw new IllegalStateException("Multiple matches between " +
                         "the same participants are not supported");
             };
        } else {
            throw new IllegalStateException("players " + pa + " and "
                    + pb + " are not both in a group or play off");
        }
        return this;
    }

    public TournamentScenario pause(Player pa, Player pb, Pause when) {
        pauseOnMatches.put(new HashSet<>(asList(pa, pb)), when);
        return this;
    }

    public TournamentScenario win(Player pa, Player pb) {
        return w31(pa, pb);
    }

    public TournamentScenario w31(Player pa, Player pb) {
        return match(pa, W31, pb);
    }

    public TournamentScenario w30(Player pa, Player pb) {
        return match(pa, W30, pb);
    }

    public TournamentScenario w32(Player pa, Player pb) {
        return match(pa, W32, pb);
    }

    public TournamentScenario lose(Player pa, Player pb) {
        return l13(pa, pb);
    }

    public TournamentScenario l03(Player pa, Player pb) {
        return match(pa, L03, pb);
    }

    public TournamentScenario l13(Player pa, Player pb) {
        return match(pa, L13, pb);
    }

    public TournamentScenario l23(Player pa, Player pb) {
        return match(pa, L23, pb);
    }

    public TournamentScenario quitsGroup(Player... ps) {
        playOffPlayers.addAll(asList(ps));
        return this;
    }

    public TournamentScenario champions(Player... ps) {
        champions.addAll(asList(ps));
        champions.forEach(player -> {
            if (!playOffPlayers.contains(player)) {
                throw new IllegalStateException("Player "
                        + player + " did not quit group");
            };
        });
        return this;
    }

    public TournamentScenario category(PlayerCategory c, Player... ps) {
        categoryDbId.put(c, null);
        playersByCategories.putAll(c, asList(ps));
        asList(ps).forEach(p ->
                ofNullable(playersCategory.putIfAbsent(p, c))
                        .ifPresent(oldCat -> {
                            throw new IllegalStateException("player "
                                    + p + " is at category " + oldCat);
                        }));
        return this;
    }
}
