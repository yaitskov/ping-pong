import SimpleController from 'core/angular/SimpleController.js';

export default class RenameParticipantCtrl extends SimpleController {
    static get $inject() {
        return ['$routeParams', 'Participant', 'AjaxInfo', 'mainMenu', '$scope'];
    }

    rename() {
        this.form.$setSubmitted();
        if (!this.form.$valid) {
            return;
        }
        this.ajax.doAjax('saving',
                         this.Participant.rename,
                         {tid: this.$routeParams.tournamentId,
                          bid: this.$routeParams.participantId,
                          expectedName: this.expectedName,
                          newName: this.fullName
                         },
                         (ok) => history.back());
    }

    $onInit() {
        this.mainMenu.setTitle('Rename participant');
        this.ajax = this.AjaxInfo.scope(this.$scope);
        this.ajax.doAjax(
            'Loading participant',
            this.Participant.profile,
            {participantId: this.$routeParams.participantId,
             tournamentId: this.$routeParams.tournamentId},
            (state) => {
                this.fullName = state.name;
                this.expectedName = this.fullName;
            });
    }
}
