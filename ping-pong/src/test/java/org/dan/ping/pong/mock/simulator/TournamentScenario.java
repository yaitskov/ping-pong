package org.dan.ping.pong.mock.simulator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.mock.simulator.MatchOutcome.L01;
import static org.dan.ping.pong.mock.simulator.MatchOutcome.L13;
import static org.dan.ping.pong.mock.simulator.MatchOutcome.L23;
import static org.dan.ping.pong.mock.simulator.MatchOutcome.W10;
import static org.dan.ping.pong.mock.simulator.MatchOutcome.W30;
import static org.dan.ping.pong.mock.simulator.MatchOutcome.W31;
import static org.dan.ping.pong.mock.simulator.MatchOutcome.W32;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.match.OpenMatchForJudge;
import org.dan.ping.pong.app.tournament.MatchValidationRule;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.mock.SessionAware;
import org.dan.ping.pong.mock.TestUserSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Getter
@ToString(of = {"uidPlayer", "categoryDbId", "tid", "placeId", "params"})
public class TournamentScenario implements SessionAware {
    private Optional<String> name = empty();
    private final Multimap<Set<Player>, GameEnd> groupMatches = ArrayListMultimap.create();
    private final Multimap<Set<Player>, GameEnd> playOffMatches = ArrayListMultimap.create();
    private final Multimap<PlayerCategory, Player> playersByCategories = HashMultimap.create();
    private final Map<Player, PlayerCategory> playersCategory = new HashMap<>();
    private final Set<Player> playOffPlayers = new HashSet<>();
    private final Map<PlayerCategory, List<Player>> champions = new HashMap<>();
    private final Map<Player, TestUserSession> playersSessions = new HashMap<>();
    private final Map<Integer, Player> uidPlayer = new LinkedHashMap<>();
    private final Map<PlayerCategory, Integer> categoryDbId = new HashMap<>();
    private final Map<Set<Player>, PlayHook> hooksOnMatches = new HashMap<>();
    private final Map<Player, EnlistMode> playerPresence = new HashMap<>();
    private final List<HookCallbackPro> onBeforeAnyMatch = new ArrayList<>();
    private final Map<Player, ProvidedRank> providedRanks = new HashMap<>();

    private boolean begin = true;
    @Setter
    private int tid;
    @Setter
    private int placeId;
    private int tables = 1;
    @Setter
    private TestUserSession testAdmin;

    public String getSession() {
        return testAdmin.getSession();
    }

    private TournamentRules rules;

    private Optional<Consumer<TournamentScenario>> onFailure = empty();

    private boolean ignoreUnexpectedGames;

    private Optional<AutoResolution> autoResolution = Optional.empty();

    public TournamentScenario onFailure(Consumer<TournamentScenario> cb){
        this.onFailure = Optional.of(cb);
        return this;
    }

    public TournamentScenario rank(ProvidedRank rank, Player... players) {
        Stream.of(players).forEach(player -> providedRanks.put(player, rank));
        return this;
    }

    public TournamentScenario autoResolution(AutoResolution resolution) {
        this.autoResolution = Optional.of(resolution);
        return this;
    }

    public TournamentScenario ignoreUnexpectedGames() {
        this.ignoreUnexpectedGames = true;
        return this;
    }

    public TournamentScenario tables(int tables) {
        this.tables = tables;
        return this;
    }

    public static TournamentScenario begin() {
        return new TournamentScenario();
    }

    public TournamentScenario rules(TournamentRules rules) {
        this.rules = rules;
        return this;
    }

    public Multimap<Set<Player>, GameEnd> chooseMatchMap(OpenMatchForJudge openMatch) {
        return openMatch.getType() == Grup
                ? getGroupMatches()
                : getPlayOffMatches();
    }

    public TournamentScenario name(String namePrefix) {
        this.name = Optional.of(namePrefix);
        return this;
    }

    private TournamentScenario match(Player pa, MatchOutcome outcome, Player pb,
            SetGenerator setGenerator) {
        playersSessions.put(pa, null);
        playersSessions.put(pb, null);
        GameEnd match = GameEnd.game(pa, outcome, pb, setGenerator);
        if (!playOffPlayers.contains(pa) && !playOffPlayers.contains(pb)) {
            final HashSet<Player> key = new HashSet<>(match.getParticipants());
            checkArgument(!groupMatches.containsKey(key),
                    "Multiple matches between "
                    + "the same participants are not supported. Check "
                    + "%s and %s", pa ,pb);
            groupMatches.put(key, match);
        } else if (playOffPlayers.contains(pa) && playOffPlayers.contains(pb)) {
             playOffMatches.put(new HashSet<>(match.getParticipants()), match);
        } else {
            throw new IllegalStateException("players " + pa + " and "
                    + pb + " are not both in a group or play off");
        }
        return this;
    }

    public TournamentScenario pause(Player pa, Player pb, Hook when) {
        return pause(pa, pb,
                PlayHook.builder()
                        .type(when)
                        .callback((scenario, players) -> Hook.pause(when, players))
                        .build());
    }

    public TournamentScenario pause(Player pa, Player pb, PlayHook when) {
        hooksOnMatches.put(new HashSet<>(asList(pa, pb)), when);
        return this;
    }

    public TournamentScenario onBeforeMatch(HookCallbackPro playHook) {
        onBeforeAnyMatch.add(playHook);
        return this;
    }

    public TournamentScenario win(Player pa, Player pb) {
        return match(pa,
                new MatchOutcome(getRules().getMatch().getSetsToWin(), 0),
                pb, getRules().getMatch());
    }

    public TournamentScenario w31(Player pa, Player pb) {
        return match(pa, W31, pb, getRules().getMatch());
    }

    public TournamentScenario w10(Player pa, Player pb) {
        return match(pa, W10, pb, getRules().getMatch());
    }

    public TournamentScenario custom(SetGenerator generator) {
        return match(generator.getPlayerA(), W31, generator.getPlayerB(), generator);
    }

    public TournamentScenario w30(Player pa, Player pb) {
        return match(pa, W30, pb, getRules().getMatch());
    }

    public TournamentScenario w32(Player pa, Player pb) {
        return match(pa, W32, pb, getRules().getMatch());
    }

    public TournamentScenario lose(Player pa, Player pb) {
        return match(pa, new MatchOutcome(0, getRules().getMatch().getSetsToWin()),
                pb, getRules().getMatch());
    }

    public TournamentScenario l13(Player pa, Player pb) {
        return match(pa, L13, pb, getRules().getMatch());
    }

    public TournamentScenario l23(Player pa, Player pb) {
        return match(pa, L23, pb, getRules().getMatch());
    }

    public TournamentScenario l01(Player pa, Player pb) {
        return match(pa, L01, pb, getRules().getMatch());
    }

    public TournamentScenario match(
            Player pa, MatchOutcome outcome, Player pb,
            MatchValidationRule matchRules) {
        return match(pa, outcome, pb,
                createRndGen(pa, outcome, pb, matchRules));
    }

    public static RndSetGenerator createRndGen(Player pa, MatchOutcome outcome, Player pb,
            MatchValidationRule matchRules) {
        return new RndSetGenerator(
                ImmutableMap.of(pa, outcome.first(), pb, outcome.second()),
                matchRules);
    }

    public TournamentScenario quitsGroup(Player... ps) {
        playOffPlayers.addAll(asList(ps));
        return this;
    }

    public TournamentScenario champions(PlayerCategory category, Player... ps) {
        champions.put(category, asList(ps));
        asList(ps).forEach(player -> {
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

    public TournamentScenario presence(EnlistMode mode, Player... players) {
        asList(players).forEach(player -> playerPresence.put(player, mode));
        return this;
    }

    public TournamentScenario doNotBegin() {
        begin = false;
        return this;
    }
}
