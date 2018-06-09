package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentResource.DRAFTING;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.marshaling.TournamentMarshalingResource.TOURNAMENT_EXPORT_STATE;
import static org.dan.ping.pong.app.tournament.marshaling.TournamentMarshalingResource.TOURNAMENT_IMPORT_STATE;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.match.rule.reason.F2fReason;
import org.dan.ping.pong.app.tournament.marshaling.ImportTournamentState;
import org.dan.ping.pong.app.tournament.marshaling.TournamentEnvelope;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.dan.ping.pong.util.time.Clocker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentMarshalingJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Inject
    private Clocker clocker;

    @Test
    public void marshalTournamentOf3InTheMiddle() {
        final TournamentScenario scenario = begin()
                .name("marshalTournamentOf3InMid")
                .rules(RULES_G8Q2_S1A2G11)
                .category(c1, p1, p2, p3);

        final ImperativeSimulator is = isf.create(scenario);
        is.run(c -> c.beginTournament()
                .scoreSet(p1, 11, p3, 0)
                .scoreSet(p2, 11, p1, 1)
                .scoreSet(p3, 11, p2, 2));

        final TournamentEnvelope expTournament = exportTour(scenario);

        assertThat(expTournament.getExportedAt(),
                lessThanOrEqualTo(clocker.get()));

        final Tid exportedTid = importTour(scenario, expTournament);

        assertThat(scenario.getTid(), lessThan(exportedTid));

        checkImportedTournamentHasSameResults(is, exportedTid);
    }

    private void checkImportedTournamentHasSameResults(ImperativeSimulator is, Tid exportedTid) {
        final DraftingTournamentInfo importedDrafting = myRest()
                .get(DRAFTING + exportedTid, DraftingTournamentInfo.class);
        final int importedCid = importedDrafting.getCategories().stream().findFirst()
                .map(CategoryLink::getCid).get();
        List<TournamentResultEntry> imported = resetUids(
                is.getTournamentResult(importedCid, exportedTid.getTid()));
        List<TournamentResultEntry> original = resetUids(is.getTournamentResult());
        assertThat(imported.size(), is(original.size()));
        for (int i = 0; i < imported.size(); ++i) {
            final TournamentResultEntry oi = original.get(i);
            final TournamentResultEntry ii = imported.get(i);
            assertThat("item " + i,  ii, allOf(
                    hasProperty("user",
                            allOf(
                                    hasProperty("uid", not(oi.getUser().getUid())),
                                    hasProperty("name", is(oi.getUser().getName())))),
                    hasProperty("state", is(oi.getState())),
                    hasProperty("playOffStep", is(oi.getPlayOffStep())),
                    hasProperty("reasonChain", is(oi.getReasonChain()))));
        }
    }

    private List<TournamentResultEntry> resetUids(List<TournamentResultEntry> entries) {
        entries.stream()
                .map(TournamentResultEntry::getReasonChain)
                .flatMap(List::stream)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(item -> item instanceof F2fReason)
                .map(F2fReason.class::cast)
                .forEach(reason -> reason.setUid(null));
        return entries;
    }

    private TournamentEnvelope exportTour(TournamentScenario scenario) {
        return myRest()
                .get(TOURNAMENT_EXPORT_STATE + scenario.getTid(),
                        TournamentEnvelope.class);
    }

    private Tid importTour(TournamentScenario scenario, TournamentEnvelope expTournament) {
        return myRest()
                .post(
                        TOURNAMENT_IMPORT_STATE,
                        scenario.getTestAdmin(),
                        ImportTournamentState
                                .builder()
                                .placeId(scenario.getPlaceId())
                                .tournament(expTournament)
                                .build())
                .readEntity(Tid.class);
    }
}
