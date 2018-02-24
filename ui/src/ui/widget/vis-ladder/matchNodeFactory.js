import matchLabel from './matchLabel.js';

export default function (participantNames, match) {
    return {
        id: match.id,
        level: match.level,
        label: matchLabel(participantNames, match),
        shape: 'box'
    };
}
