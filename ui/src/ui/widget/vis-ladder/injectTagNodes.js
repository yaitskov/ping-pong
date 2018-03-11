import mapO from 'core/collection/mapO.js';
import matchNodeFactory from './matchNodeFactory.js';

export default function (tournament) {
    const ds = matchNodeFactory(tournament);
    ds.add(mapO(tournament.rootTaggedMatches,
                (rootTagMatch) => ({
                    id: `tag${rootTagMatch.mid}`,
                    shape: 'box',
                    level: rootTagMatch.level + 1,
                    label: `Level ${rootTagMatch.tag.number}`,
                    color: {
                        background: 'orange',
                        border: 'black'
                    }
                }),
                []));
    return ds;
}
