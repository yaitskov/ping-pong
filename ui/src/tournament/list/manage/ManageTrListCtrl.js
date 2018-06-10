import SimpleController from 'core/angular/SimpleController.js';

export default class ManageTrListCtrl extends SimpleController {
    static get $inject() {
        return ['Tournament', 'mainMenu', '$rootScope',
                'binder', '$scope', 'requestStatus'];
    }

    $onInit() {
        this.mainMenu.setTitle(
            'AdministratedTournaments',
            {
                '#!/tournament/new': {
                    text: 'AddTournament',
                    glyph: 'plus'
                },
                '#!/tournament/import': {
                    text: 'import-tournament-from-cs-backup',
                    glyph: 'cloud-upload'
                }
            });

        this.binder(this.$scope, {
            'event.request.status.ready': (e) => {
                this.requestStatus.startLoading();
                this.Tournament.administered(
                    {completeInDays: 30},
                    (tournaments) => {
                        this.requestStatus.complete();
                        this.tournaments = tournaments;
                    },
                    (...a) => this.requestStatus.failed(...a));
            }
        });
    }
}
