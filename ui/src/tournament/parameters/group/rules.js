import arrayEnum from 'core/collection/arrayEnum.js';
import MatchOutcomeScope from './MatchOutcomeScope.js';
import MatchParticipantScope from './MatchParticipantScope.js';

const ruleId = arrayEnum('BB', 'WM', 'f2f', 'LB', 'LS', 'rnd', 'SB', 'WB', 'WS', 'Punkts', 'DM');
export ruleId;

export function createGroupRule(type, options) {
    return Object.assign({'@type': type}, options || {});
}

function createMatchBasedRule(type, options) {
    return createGroupRule(
        type,
        Object.assign(
            {
                matchParticipantScope: MatchParticipantScope.AT_LEAST_ONE,
                matchOutcomeScope: MatchOutcomeScope.ALL_MATCHES
            },
            options || {}
        ));
}

const matchBasedRules = [ruleId.BB, ruleId.WM, ruleId.LB, ruleId.LS,
                         ruleId.SB, ruleId.WB, ruleId.WS, ruleId.Punkts].map(
                             rlId => (o) => createMatchBasedRule(rlId, o));

const customRules = [
    (o) => createGroupRule(
        ruleId.f2f,
        Object.assign(
            {matchOutcomeScope: MatchOutcomeScope.ALL_MATCHES},
            o)),
    (o) => createGroupRule(ruleId.rnd, o),
    (o) => createGroupRule(ruleId.DM, o)
];

const ruleType2Factory = matchBasedRules.concat(customRules).map(
    ruleFactory => [ruleFactory()['@type'], ruleFactory]);

export ruleType2Factory;