const defaultMatchRules = {
    PingPong: {
        '@type': 'PingPong',
        minGamesToWin: 11,
        minAdvanceInGames: 2,
        minPossibleGames: 0,
        setsToWin: 3
    },
    Tennis: {
        '@type': 'Tennis',
        minGamesToWin: 6,
        minAdvanceInGames: 2,
        minPossibleGames: 0,
        setsToWin: 2,
        superTieBreakGames: 10
    }
};

export default defaultMatchRules;