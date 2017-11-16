package org.dan.ping.pong.mock.simulator;

import org.dan.ping.pong.mock.MyRest;
import org.dan.ping.pong.mock.RestEntityGenerator;

import javax.inject.Inject;

public class ImperativeSimulatorFactory {
    @Inject
    private Simulator simulator;
    @Inject
    private RestEntityGenerator restGenerator;
    @Inject
    private MyRest myRest;

    public ImperativeSimulator create(String name) {
        return create(TournamentScenario.begin().name(name));
    }

    public ImperativeSimulator create(TournamentScenario baseScenario) {
        return new ImperativeSimulator(simulator,
                restGenerator,
                baseScenario.ignoreUnexpectedGames(),
                myRest);
    }
}
