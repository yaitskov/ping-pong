import SimpleController from 'core/angular/SimpleController.js';

const BidTerminalStates = new Set(['Quit', 'Expl', 'Win1', 'Win2', 'Win3']);

export default class ManageOneCtrl extends SimpleController {
    static get $inject() {
        return ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                'Participant', 'binder', '$scope', '$rootScope'];
    }

    expel() {
        this.participant.tid = this.tournamentId;
        this.$rootScope.$broadcast('event.confirm-participant-expel.confirm', this.participant);
    }

    $onInit() {
        this.tournamentId = this.$routeParams.tournamentId;
        this.participant = null;
        this.BidTerminalStates = new Set(['Quit', 'Expl', 'Win1', 'Win2', 'Win3']);
        this.binder(this.$scope, {
            'event.main.menu.ready': (e) => {
                var ctxMenu = {};
                ctxMenu['#!/my/tournament/' + this.$routeParams.tournamentId] = 'Tournament';
                this.mainMenu.setTitle('Participant', ctxMenu);
            },
            'event.request.status.ready': (event) => {
                this.requestStatus.startLoading('Loading participant');
                this.Participant.state(
                    {uid: this.$routeParams.userId,
                     tournamentId: this.$routeParams.tournamentId},
                    (state) => {
                        this.requestStatus.complete();
                        this.participant = state;
                    },
                    this.requestStatus.failed);
            }
        });
    }
}
