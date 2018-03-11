import mapO from 'core/collection/mapO.js';
import matchEdgeFactory from './matchEdgeFactory.js';

export default function (tournament) {
    const ds = matchEdgeFactory(tournament);
    ds.add(mapO(tournament.rootTaggedMatches,
                (rootTagMatch) => ({
                    from: rootTagMatch.mid,
                    to: `tag${rootTagMatch.mid}`,
                    arrow: 'to'
                }),
                []));
    return ds;
}
