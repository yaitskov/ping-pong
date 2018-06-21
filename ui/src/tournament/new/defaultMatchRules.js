export default function defaultMatchRules(sport) {
    switch (sport) {
    case undefined:
    case 'PP':
        return {
            '@type': 'PP',
            mgtw: 11,
            maig: 2,
            mpg: 0,
            stw: 3,
            cos: false
        };
    case 'TE':
        return {
            '@type': 'TE',
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
