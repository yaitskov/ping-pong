import SimpleController from 'core/angular/SimpleController.js';

export default class ManageOneCtrl extends SimpleController {
    static get $inject() {
        return ['AjaxInfo', 'mainMenu', '$routeParams',
                'Participant', '$scope', '$rootScope'];
    }

    expel() {
        this.participant.tid = this.tournamentId;
        this.participant.bid = this.participantId;
        this.$rootScope.$broadcast('event.confirm-participant-expel.confirm', this.participant);
    }

    $onInit() {
        this.ajax = this.AjaxInfo.scope(this.$scope);
        this.tournamentId = this.$routeParams.tournamentId;
        this.participantId = this.$routeParams.participantId;
        this.participant = null;
        this.BidTerminalStates = new Set(['Quit', 'Expl', 'Win1', 'Win2', 'Win3']);
        const ctxMenu = {};
        ctxMenu['#!/my/tournament/' + this.tournamentId] = 'Tournament';
        this.mainMenu.setTitle('Participant', ctxMenu);

        this.ajax.doAjax(
            'Loading participant',
            this.Participant.profile,
            {participantId: this.participantId,
             tournamentId: this.tournamentId},
            (par) => this.participant = par);
    }
}
