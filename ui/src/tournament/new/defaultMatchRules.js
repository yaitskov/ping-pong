export default function defaultMatchRules(sport) {
    switch (sport) {
    case undefined:
    case 'PingPong':
        return {
            '@type': 'PingPong',
            minGamesToWin: 11,
            minAdvanceInGames: 2,
            minPossibleGames: 0,
            setsToWin: 3,
            countOnlySets: false
        };
    case 'Tennis':
        return {
            '@type': 'Tennis',
            minGamesToWin: 6,
            minAdvanceInGames: 2,
            minPossibleGames: 0,
            setsToWin: 2,
            superTieBreakGames: 10
        };
    default:
        throw new Error(`Sport [${sport}] is not supported`);
    }
}
