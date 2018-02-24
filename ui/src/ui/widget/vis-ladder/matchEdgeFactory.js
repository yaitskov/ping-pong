import vis from 'vis';

export default function (tournament) {
    return new vis.DataSet(
        (tournament.transitions || []).
            map((edge, idx) => {
                return {...edge, id: 1 + idx, arrow: 'to'};
            }));
}
