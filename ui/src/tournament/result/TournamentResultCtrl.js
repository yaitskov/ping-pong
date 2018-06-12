import SimpleController from 'core/angular/SimpleController.js';

export default class TournamentResult extends SimpleController {
    static get $inject() {
        return ['Tournament', 'mainMenu', '$routeParams',
                'requestStatus', 'binder', '$scope'];
    }

    $onInit() {
        this.matches = null;
        this.winners = null;
        this.tournament = null;
        this.currentCid = null;
        this.tid = this.$routeParams.tournamentId;
        this.mainMenu.setTitle('Tournament results');
        const params = {tournamentId: this.$routeParams.tournamentId};
        this.binder(this.$scope, {
            'event.request.status.ready': (event) => {
                this.requestStatus.startLoading();
                this.Tournament.aComplete(
                    {tournamentId: this.$routeParams.tournamentId},
                    (tournament) => {
                        this.requestStatus.complete();
                        this.tournament = tournament;
                        tournament.tid = this.$routeParams.tournamentId;
                        for (var i in tournament.categories) {
                            var category = tournament.categories[i];
                            this.pickCategory(category.cid);
                            break;
                        }
                    },
                    (...r) => this.requestStatus.failed(...r));
            }
        });
    }

    splitPlayOffAndGroupParticipants() {
        var border = -1;
        this.inGroupParticipants = null;
        this.playOffParticipants = null;
        for (var i = 1; i < this.participants.length; ++i) {
            if (this.participants[i - 1].playOffStep && !this.participants[i].playOffStep) {
                border = i;
                break;
            }
        }
        if (border < 0) {
            this.playOffParticipants = this.participants;
        } else {
            this.playOffParticipants = this.participants.slice(0, border);
            this.inGroupParticipants = this.participants.slice(border);
        }
        this.numOfPlayOffParticipants = this.playOffParticipants.length;
    }

    pickCategory(cid) {
        this.requestStatus.startLoading("Loading participants");
        this.currentCid = cid;
        this.Tournament.result(
            {tournamentId: this.$routeParams.tournamentId,
             categoryId: cid},
            (participants) => {
                this.requestStatus.complete();
                this.currentCid = cid;
                this.participants = participants;
                this.splitPlayOffAndGroupParticipants();
            },
            this.requestStatus.failed);
    }

}
