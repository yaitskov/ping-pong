import NodeMenu from './NodeMenu.js';

export default class MatchManagementLinkFactory {
    constructor(tournamentId) {
        this.tournamentId = tournamentId;
    }

    create(nodeId) {
        return new NodeMenu(nodeId,
                            $(`<div class="vis-node-menu">
                                <a class="btn btn-default" title="manage the match"
                                   href="#!/match/management/${this.tournamentId}/${nodeId}">
                                   <span class="utf-icon">⚙</span>
                                </a>
                                <a class="btn btn-primary" title="score the match"
                                   href="#!/participant/score/set/${this.tournamentId}/${nodeId}">
                                   <span class="glyphicon glyphicon-bullhorn"></span>
                                </a>
                                <a class="btn btn-warning"
                                   title="alter set score"
                                   href="#!/match/edit-score/${this.tournamentId}/${nodeId}">
                                   <span class="glyphicon glyphicon-pencil"></span>
                                </a>
                              </div>`));
    }
}
