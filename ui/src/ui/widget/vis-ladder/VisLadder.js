import vis from 'vis';
import NodeMenuManager from 'ui/vis-popup-menu/NodeMenuManager.js';
import MatchManagementLinkFactory from 'ui/vis-popup-menu/MatchManagementLinkFactory.js';
import playOffVisLadderOptions from './playOffVisLadderOptions.js';
import injectTagNodes from './injectTagNodes.js';
import injectTagEdges from './injectTagEdges.js';

export default class VisLadder {
    get options() {
        return playOffVisLadderOptions();
    }

    data(tournament) {
        return {
            nodes: injectTagNodes(tournament),
            edges: injectTagEdges(tournament)
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
