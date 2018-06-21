export default function defaultMatchRules(sport) {
    switch (sport) {
    case undefined:
    case 'PingPong':
        return {
            '@type': 'PingPong',
            mgtw: 11,
            maig: 2,
            mpg: 0,
            stw: 3,
            cos: false
        };
    case 'Tennis':
        return {
            '@type': 'Tennis',
            mgtw: 6,
            maig: 2,
            mpg: 0,
            stw: 2,
            stbg: 10
        };
    default:
        throw new Error(`Sport [${sport}] is not supported`);
    }
}
