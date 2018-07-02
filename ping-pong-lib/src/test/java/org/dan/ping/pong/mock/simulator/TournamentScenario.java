package org.dan.ping.pong.mock.simulator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
import static org.junit.Assert.assertNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.OpenMatchForJudge;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.TournamentState;
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
@ToString(of = {"bidPlayer", "categoryDbId", "tid", "placeId", "params"})
public class TournamentScenario implements SessionAware {
    private SportType sport = SportType.PingPong;
    private Optional<String> name = empty();
    private PlayerCategory defaultCategory;
    private final Multimap<Set<Player>, GameEnd> groupMatches = ArrayListMultimap.create();
    private final Multimap<Set<Player>, GameEnd> playOffMatches = ArrayListMultimap.create();
    private final Multimap<PlayerCategory, Player> playersByCategories = HashMultimap.create();
    private final Map<Player, List<PlayerCategory>> playersCategory = new HashMap<>();
    private final Set<Player> playOffPlayers = new HashSet<>();
    private final Map<PlayerCategory, List<Player>> champions = new HashMap<>();
    private final Map<Player, TestUserSession> playersSessions = new HashMap<>();
    private final Map<Bid, Player> bidPlayer = new LinkedHashMap<>();
    private final Map<PlayerCategory, Integer> categoryDbId = new HashMap<>();
    private final Map<Set<Player>, PlayHook> hooksOnMatches = new HashMap<>();
    private final Table<Player, PlayerCategory, EnlistMode> playerPresence = HashBasedTable.create();
    private final List<HookCallbackPro> onBeforeAnyMatch = new ArrayList<>();
    private final Table<Player, PlayerCategory, ProvidedRank> providedRanks = HashBasedTable.create();

    private TournamentState expectedTerminalState = TournamentState.Close;

    public TournamentScenario terminalState(TournamentState state) {
        expectedTerminalState = state;
        return this;
    }

    private boolean begin = true;
    @Setter
    private Tid tid;
    private Optional<Tid> consoleTid = Optional.empty();
    @Setter
    private Pid placeId;
    private int tables = 1;
    @Setter
    private TestUserSession testAdmin;

    public TournamentScenario consoleTid(Tid tid) {
        consoleTid = Optional.of(tid);
        return this;
    }

    public TestUserSession findSession(Player p) {
        return playersSessions.get(p);
    }

    public Bid findBid(Player p) {
        final Map<PlayerCategory, Bid> catBid = playersSessions.get(p).getCatBid();
        if (catBid.size() == 1){
            return catBid.get(defaultCategory);
        }
        throw new IllegalStateException("Ambiguous category for " + p);
    }

    public TournamentScenario createConsoleTournament() {
        final TournamentScenario result = new TournamentScenario();
        result.setTestAdmin(testAdmin);
        result.setPlaceId(placeId);
        result.defaultCategory = defaultCategory;
        result.getBidPlayer().putAll(bidPlayer);
        result.getPlayersByCategories().putAll(playersByCategories);
        result.name(name.get()).tables(tables).sport(sport)
                .getPlayersSessions().putAll(playersSessions);
        result.setTid(consoleTid.get());
        // category mapping is not know yet
        return result;
    }

    public String getSession() {
        return testAdmin.getSession();
    }

    public Bid player2Bid(Player p) {
        return player2Bid(p, defaultCategory);
    }

    public Bid player2Bid(Player p, PlayerCategory category) {
        return ofNullable(playersSessions.get(p))
                .flatMap(tus -> ofNullable(tus.getCatBid().get(category)))
                .orElseThrow(() -> new RuntimeException("No bid for player " + p
                        + " in category " + category));
    }

    private TournamentRules rules;

    private Optional<Consumer<TournamentScenario>> onFailure = empty();

    private boolean ignoreUnexpectedGames;

    private Optional<AutoResolution> autoResolution = Optional.empty();

    public TournamentScenario onFailure(Consumer<TournamentScenario> cb){
        this.onFailure = Optional.of(cb);
        return this;
    }

    public TournamentScenario sport(SportType type) {
        sport = type;
        return this;
    }

    public TournamentScenario rank(ProvidedRank rank, Player... players) {
        Stream.of(players).forEach(player -> providedRanks.put(player, defaultCategory, rank));
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
        return openMatch.getMatchType() == Grup
                ? getGroupMatches()
                : getPlayOffMatches();
    }

    public TournamentScenario name(String namePrefix) {
        this.name = Optional.of(namePrefix);
        return this;
    }

    private TournamentScenario match(Player pa, MatchOutcome outcome, Player pb,
            SetGenerator setGenerator) {
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
        return match(pa, OutComeGenerator.generateWin(getRules().getMatch()), pb);
    }

    public TournamentScenario w31(Player pa, Player pb) {
        return match(pa, W31, pb);
    }

    public TournamentScenario w10(Player pa, Player pb) {
        return match(pa, W10, pb);
    }

    public TournamentScenario custom(SetGenerator generator) {
        return match(generator.getPlayerA(), W31, generator.getPlayerB(), generator);
    }

    public TournamentScenario w30(Player pa, Player pb) {
        return match(pa, W30, pb);
    }

    public TournamentScenario w32(Player pa, Player pb) {
        return match(pa, W32, pb);
    }

    public TournamentScenario lose(Player pa, Player pb) {
        return match(pa, OutComeGenerator.generateLose(getRules().getMatch()), pb);
    }

    public TournamentScenario l13(Player pa, Player pb) {
        return match(pa, L13, pb);
    }

    public TournamentScenario l23(Player pa, Player pb) {
        return match(pa, L23, pb);
    }

    public TournamentScenario l01(Player pa, Player pb) {
        return match(pa, L01, pb);
    }

    public TournamentScenario match(
            Player pa, MatchOutcome outcome, Player pb) {
        return match(pa, outcome, pb, createRndGen(pa, outcome, pb));
    }

    public static RndSetGenerator createRndGen(Player pa, MatchOutcome outcome, Player pb) {
        return new RndSetGenerator(
                ImmutableMap.of(pa, outcome.first(), pb, outcome.second()));
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

    public TournamentScenario cat(PlayerCategory c) {
        defaultCategory = c;
        return this;
    }

    private void registerPlayerCat(Player p, PlayerCategory c) {
        ofNullable(playersCategory.putIfAbsent(p, new ArrayList<>(singletonList(c))))
                .ifPresent(oldCat -> {
                    if (oldCat.contains(c)) {
                        throw new IllegalStateException("player "
                                + p + " is already in category " + oldCat);
                    }
                    oldCat.add(c);
                });
    }

    public TournamentScenario category(PlayerCategory c, Player... ps) {
        defaultCategory = c;
        categoryDbId.put(c, null);
        playersByCategories.putAll(c, asList(ps));
        asList(ps).forEach(p -> registerPlayerCat(p, c));
        return this;
    }

    public TournamentScenario presence(EnlistMode mode, Player... players) {
        asList(players).forEach(player -> playerPresence.put(player, defaultCategory, mode));
        return this;
    }

    public TournamentScenario doNotBegin() {
        begin = false;
        return this;
    }

    public void addPlayer(Bid bid, Player player) {
        assertNull(getBidPlayer().put(bid, player));
        registerPlayerCat(player, defaultCategory);
        playersSessions.put(player, TestUserSession.builder()
                .catBid(new HashMap<>(ImmutableMap.of(defaultCategory, bid)))
                .player(player)
                .name(name +  "_" + player)
                .email(name + "_" + player + "@gmail.com")
                .build());
    }
}
