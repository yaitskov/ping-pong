import { ruleId, ruleType2Factory } from './rules.js';

function makeRules(ruleIds) {
    return () => ruleIds.map(ri => ruleType2Factory.get(ri)());
}

function selectRuleIds(sport) {
    switch(sport) {
    case 'TE':
        return [ruleId.WM, ruleId.f2f,
                ruleId.SB, ruleId.f2f,
                ruleId.BB, ruleId.f2f,
                ruleId.rnd
               ];
    case 'PP':
        return [ruleId.Punkts, ruleId.f2f,
                ruleId.SB, ruleId.f2f,
                ruleId.BB, ruleId.f2f,
                ruleId.DM,
                ruleId.Punkts, ruleId.f2f,
                ruleId.SB, ruleId.f2f,
                ruleId.BB, ruleId.f2f,
                ruleId.rnd
               ];
    default:
        throw new Error(`Sport [${sport}] is not supported`);
    }
}

export default function (sport) {
    const ruleIds = selectRuleIds(sport);
    return ruleIds.map(ri => ruleType2Factory.get(ri)());
};
