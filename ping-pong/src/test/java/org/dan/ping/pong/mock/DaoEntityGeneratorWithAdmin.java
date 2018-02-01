package org.dan.ping.pong.mock;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BalancedMix;
import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BestToBest;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.Manual;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.ProvidedRating;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.SignUp;
import static org.dan.ping.pong.app.match.MatchJerseyTest.GLOBAL;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing1;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.mock.Generators.genStr;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.castinglots.rank.OrderDirection;
import org.dan.ping.pong.app.castinglots.rank.ProvidedRankOptions;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.TournamentState;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DaoEntityGeneratorWithAdmin {
    public static final CastingLotsRule INCREASE_SIGNUP_CASTING
            = CastingLotsRule.builder()
            .policy(SignUp)
            .direction(OrderDirection.Increase)
            .splitPolicy(BestToBest)
            .build();

    public static final CastingLotsRule INCREASE_SIGNUP_MIX
            = CastingLotsRule.builder()
            .policy(SignUp)
            .direction(OrderDirection.Increase)
            .splitPolicy(BalancedMix)
            .build();

    public static final CastingLotsRule BALANCED_MANUAL
            = CastingLotsRule.builder()
            .policy(Manual)
            .direction(OrderDirection.Increase)
            .splitPolicy(BalancedMix)
            .build();

    public static final CastingLotsRule INCREASE_PROVIDED_RANKING
            = CastingLotsRule.builder()
            .policy(ProvidedRating)
            .direction(OrderDirection.Increase)
            .splitPolicy(BestToBest)
            .providedRankOptions(Optional.of(ProvidedRankOptions
                    .builder()
                    .label("TEST")
                    .minValue(1)
                    .maxValue(10)
                    .build()))
            .build();

    private final TestAdmin testAdmin;
    private final DaoEntityGenerator daoEntityGenerator;

    public Tid genTournament(Pid placeId) {
        return genTournament(placeId, Draft);
    }

    public Tid genTournament(Pid placeId, TournamentState state) {
        return genTournament(placeId, TournamentProps.builder()
                .state(state)
                .sport(SportType.PingPong)
                .rules(rules(2))
                .build());
    }

    public Tid genTournament(Pid placeId, TournamentState state, int quits) {
        return genTournament(placeId, TournamentProps.builder()
                .state(state)
                .sport(SportType.PingPong)
                .rules(rules(quits))
                .build());
    }

    private TournamentRules rules(int quits) {
        return TournamentRules.builder()
                .match(PingPongMatchRules.builder()
                        .setsToWin(3)
                        .minGamesToWin(11)
                        .minAdvanceInGames(2)
                        .build())
                .group(Optional.of(GroupRules.builder()
                        .groupSize(8)
                        .quits(quits)
                        .build()))
                .place(Optional.of(GLOBAL))
                .playOff(Optional.of(Losing1))
                .casting(INCREASE_SIGNUP_CASTING)
                .build();
    }

    public Tid genTournament(Pid placeId, TournamentProps props) {
        return daoEntityGenerator.genTournament(testAdmin.getUid(), placeId, props);
    }

    public Pid genPlace(int tables) {
        return daoEntityGenerator.genPlace(testAdmin.getUid(), tables);
    }

    public int genCategory(Tid tid) {
        return daoEntityGenerator.genCategory(UUID.randomUUID().toString(), tid);
    }

    public int genCity() {
        return daoEntityGenerator.genCity(genStr(), testAdmin.getUid());
    }
}
