import defaultMatchRules from './defaultMatchRules.js';

export default function defaultTournamentRules(sport) {
    return {
        match: defaultMatchRules(sport),
        place: {arenaDistribution: 'NO'},
        casting: {
            policy: 'ProvidedRating',
            direction: 'Decrease',
            splitPolicy: 'BalancedMix',
            providedRankOptions: {
                label: 'rating',
                minValue: 0,
                maxValue: 500000
            }
        },
        group: {
            disambiguation: 'CMP_WIN_MINUS_LOSE',
            quits: 2,
            console: 'NO',
            groupSize: 8,
            schedule: {
                size2Schedule: {
                    2: [0, 1],
                    3: [0, 2, 0, 1, 1, 2]
                }
            }
        },
        playOff: {
            losings: 1,
            thirdPlaceMatch: 1
        }
    };
}
