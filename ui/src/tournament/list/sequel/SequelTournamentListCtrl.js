import SimpleController from 'core/angular/SimpleController.js';

export default class EnlistOnlineCtrl extends SimpleController {
    static get $inject () {
        return ['Tournament', 'mainMenu', '$routeParams', 'AjaxInfo', '$scope'];
    }

    $onInit() {
        this.mainMenu.setTitle('Following Tournaments');
        this.AjaxInfo.scope(this.$scope).
            doAjax('Loading tournaments...',
                   this.Tournament.following,
                   {tournamentId: this.$routeParams.tournamentId},
                   (tournaments) => this.tournaments = tournaments);
    }

    viewUrl(tournament) {
        throw new Error('Implement me');
    }
}
