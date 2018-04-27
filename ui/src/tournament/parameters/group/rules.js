import arrayEnum from 'core/collection/arrayEnum.js';
import MatchOutcomeScope from './MatchOutcomeScope.js';
import MatchParticipantScope from './MatchParticipantScope.js';

export const ruleId = arrayEnum('BB', 'WM', 'f2f', 'LB', 'LS', 'rnd', 'SB', 'WB', 'WS', 'Punkts', 'DM');

function createGroupRule(type, options) {
    return Object.assign({'@type': type}, options || {});
}

export function createMatchBasedRule(type, options) {
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

export function createF2fRule(options) {
    return createGroupRule(
        ruleId.f2f,
        Object.assign(
            {matchOutcomeScope: MatchOutcomeScope.ALL_MATCHES},
            options || {}));
}

export function createDmRule(options) {
    return createGroupRule(
        ruleId.DM,
        Object.assign({}, options || {}));
}


const matchBasedRules = [ruleId.BB, ruleId.WM, ruleId.LB, ruleId.LS,
                         ruleId.SB, ruleId.WB, ruleId.WS, ruleId.Punkts].map(
                             rlId => (o) => createMatchBasedRule(rlId, o));

const customRules = [
    (o) => createF2fRule(o),
    (o) => createGroupRule(ruleId.rnd, o),
    (o) => createGroupRule(ruleId.DM, o)
];

// const _ruleId2Factory =
export const ruleType2Factory = new Map(matchBasedRules.concat(customRules).map(
    ruleFactory => [ruleFactory()['@type'], ruleFactory]));
