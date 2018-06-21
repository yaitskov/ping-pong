export default function defaultMatchRules(sport) {
    switch (sport) {
    case undefined:
    case 'PingPong':
        return {
            '@type': 'PingPong',
            mgtw: 11,
            maig: 2,
            mpg: 0,
            setsToWin: 3,
            countOnlySets: false
        };
    case 'Tennis':
        return {
            '@type': 'Tennis',
            mgtw: 6,
            maig: 2,
            mpg: 0,
            setsToWin: 2,
            superTieBreakGames: 10
        };
    default:
        throw new Error(`Sport [${sport}] is not supported`);
    }
}
