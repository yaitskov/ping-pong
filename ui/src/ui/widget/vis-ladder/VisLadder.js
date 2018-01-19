import vis from 'vis';
import matchLabel from './match-label.js';
import NodeMenuManager from 'ui/vis-popup-menu/NodeMenuManager.js';
import MatchManagementLinkFactory from 'ui/vis-popup-menu/MatchManagementLinkFactory.js';

export default class VisLadder {
    createMatchNode(tournament, match) {
        return {id: match.id, level: match.level,
                label: matchLabel(tournament, match),
                shape: 'box'};
    };

    get options() {
        return {
            autoResize: true,
            height: '100%',
            width: '100%',
            manipulation: false,
            nodes: {
                font: {
                    face: 'mono',
                    size: 20,
                    color: 'black',
                    align: 'left'
                },
                color: {
                    background: '#bbb',
                    border: 'black'
                },
                margin: {
                    right: 10
                }
            },
            edges: {
                arrows: 'to',
                color: {
                    color: 'green',
                    inherit: false
                }
            },
            layout: {
                hierarchical: {
                    direction: 'LR',
                    enabled: true,
                    levelSeparation: 400,
                    sortMethod: "directed"
                },
                //randomSeed: 2
            },
            physics: {
                hierarchicalRepulsion: {
                    nodeDistance: 80,
                }
            }
        };
    }

    data(tournament) {
        let nodes = [];
        for (let match of tournament.matches) {
            nodes.push(this.createMatchNode(tournament, match));
        }
        let edges = [];
        if (tournament.transitions) {
           for (let edge of tournament.transitions) {
               edge.arrow = 'to';
               edges.push(edge);
           }
        }
        return {
            nodes: new vis.DataSet(nodes),
            edges: new vis.DataSet(edges)
        };
    }

    constructor(container, tournament) {
        let options = this.options;
        let data = this.data(tournament);
        const network = new vis.Network(container, data, options);
        let menuManager = new NodeMenuManager(
            $(container),
            network,
            options,
            data.nodes,
            new MatchManagementLinkFactory(tournament.tid));
        network.once('afterDrawing', () => network.fit());
    }
}
