package org.dan.ping.pong.app.match.rule;

public enum OrderRuleName {
    Punkts,
    CountDisambiguationMatches,
    _DisambiguationPreview,

    F2F,
    Random,

    SetsBalance,
    BallsBalance,
    WonSets,
    LostSets,
    WonBalls,
    LostBalls,

    WonMatches,

    // Applicable for Play Off Only
    LostMatches,
    Level,
    CumDiffSets,
    CumDiffBalls
}
