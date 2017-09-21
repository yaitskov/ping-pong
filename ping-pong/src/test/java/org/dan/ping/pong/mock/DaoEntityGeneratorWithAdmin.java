package org.dan.ping.pong.mock;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BalancedMix;
import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BestToBest;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.ProvidedRating;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.SignUp;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing0;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.mock.Generators.genStr;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.castinglots.rank.OrderDirection;
import org.dan.ping.pong.app.castinglots.rank.ProvidedRankOptions;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.tournament.MatchValidationRule;
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

    public int genTournament(int placeId) {
        return genTournament(placeId, Draft);
    }

    public int genTournament(int placeId, TournamentState state) {
        return genTournament(placeId, TournamentProps.builder()
                .state(state)
                .rules(rules(2))
                .build());
    }

    public int genTournament(int placeId, TournamentState state, int quits) {
        return genTournament(placeId, TournamentProps.builder()
                .state(state)
                .rules(rules(quits))
                .build());
    }

    private TournamentRules rules(int quits) {
        return TournamentRules.builder()
                .match(MatchValidationRule.builder()
                        .setsToWin(3)
                        .minGamesToWin(11)
                        .minAdvanceInGames(2)
                        .build())
                .group(Optional.of(GroupRules.builder()
                        .maxSize(8)
                        .quits(quits)
                        .build()))
                .playOff(Optional.of(Losing0))
                .casting(INCREASE_SIGNUP_CASTING)
                .prizeWinningPlaces(3)
                .build();
    }

    public int genTournament(int placeId, TournamentProps props) {
        return daoEntityGenerator.genTournament(testAdmin.getUid(), placeId, props);
    }

    public int genPlace(int tables) {
        return daoEntityGenerator.genPlace(testAdmin.getUid(), tables);
    }

    public int genCategory(int tid) {
        return daoEntityGenerator.genCategory(UUID.randomUUID().toString(), tid);
    }

    public int genCity() {
        return daoEntityGenerator.genCity(genStr(), testAdmin.getUid());
    }
}
