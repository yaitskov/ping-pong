import SimpleController from 'core/angular/SimpleController.js';

export default class RenameParticipantCtrl extends SimpleController {
    static get $inject() {
        return ['$routeParams', 'Participant', 'AjaxInfo', 'mainMenu'];
    }

    rename() {
        this.AjaxInfo.doAjax('saving',
                             this.Participant.rename,
                             {tid: this.$routeParams.tournamentId,
                              uid: this.$routeParams.participantId,
                              expectedName: this.expectedName,
                              newName: this.fullName
                             },
                             (ok) => history.back());
    }

    $onInit() {
        this.mainMenu.setTitle('Rename participant');
        this.AjaxInfo.doAjax(
            'Loading participant',
            this.Participant.state,
            {uid: this.$routeParams.participantId,
             tournamentId: this.$routeParams.tournamentId},
            (state) => {
                this.fullName = state.user.name;
                this.expectedName = this.fullName;
            });
    }
}
