import vis from 'vis';
import mapO from 'core/collection/mapO.js';
import matchLabel from './matchLabel.js';

export default function (tournament) {
    return new vis.DataSet(
        mapO(tournament.matches,
             (match) => {
                 return {
                     id: match.id,
                     level: match.level,
                     label: matchLabel(tournament.participants, match),
                     shape: 'box'
                 };
             },
             [{id: 'message',
               level: 1,
               label: 'No matches in PlayOff',
               shape: 'box'}]));
}
