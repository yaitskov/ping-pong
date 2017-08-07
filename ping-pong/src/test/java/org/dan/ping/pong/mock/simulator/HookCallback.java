package org.dan.ping.pong.mock.simulator;

import java.util.function.BiFunction;

public interface HookCallback extends BiFunction<TournamentScenario, MatchMetaInfo, HookDecision> {
}
