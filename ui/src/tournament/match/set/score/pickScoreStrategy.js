import possibleScoresStrategies from './possibleScoresStrategy.js';
import possibleSetsStrategies from './possibleSetsStrategy.js';

export default function (matchRules) {
    if (matchRules.countOnlySets) {
        return possibleSetsStrategies[matchRules['@type']];
    } else {
        return possibleScoresStrategies[matchRules['@type']];
    }
}
