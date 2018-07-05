import defaultMatchRules from './defaultMatchRules.js';
import sportDefaultGroupRules from 'tournament/parameters/group/sportDefaultGroupRules.js';

export default function defaultTournamentRules(sport) {
    return {
        match: defaultMatchRules(sport),
        place: {ad: 'NO'},
        enlist: 'OT',
        casting: {
            policy: 'pr',
            direction: 'd',
            splitPolicy: 'bm',
            pro: {
                label: 'rating',
                minValue: 0,
                maxValue: 500000
            }
        },
        group: {
            quits: 2,
            console: 'NO',
            groupSize: 8,
            schedule: {
                size2Schedule: {
                    2: [0, 1],
                    3: [0, 2, 0, 1, 1, 2]
                }
            },
            orderRules: sportDefaultGroupRules(sport)
        },
        playOff: {
            losings: 1,
            tpm: 1
        }
    };
}
