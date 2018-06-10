import SimpleController from 'core/angular/SimpleController.js';

export default class ImportTournamentCtrl extends SimpleController {
    static get $inject() {
        return ['mainMenu', 'placePicker', '$http', 'auth', 'InfoPopup',
                'requestStatus', '$location', 'pageCtx', '$scope'];
    }

    $onInit() {
        this.info = this.InfoPopup.createScope();
        this.$scope.$on('$destroy', () => this.info.clearAll());

        this.mainMenu.setTitle('Tournament import');
        this.place = this.placePicker.getChosenPlace() || {};
        this.tournamentBackup = this.pageCtx.get('tournamentBackup');

        this.$scope.$watch('$ctrl.tournamentBackup', (newValue) => {
            if (newValue) {
                try {
                    this.tournamentBackupJson = JSON.parse(newValue);
                } catch (e) {
                    console.error(JSON.stringify(e));
                    this.info.transError('Backup file is not in JSON format');
                    this.tournamentBackup = null;
                    this.tournamentBackupJson = null;
                }
            }
        });

    }

    choosePlace() {
        this.pageCtx.put('tournamentBackup', this.tournamentBackup);
        this.placePicker.pickFrom();
    }

    importTournament() {
        this.form.$setSubmitted();
        if (!this.form.$valid) {
            return;
        }
        this.info.transInfo('Loading');
        this.requestStatus.startLoading();
        this.$http.post('/api/tournament/import/state',
                        {
                            placeId: this.place.pid,
                            tournament: this.tournamentBackupJson
                        },
                        {headers: {session: this.auth.mySession()}}).
            then(
                (ok) => {
                    this.info.clearAll();
                    this.info.transInfo('Tournament imported',
                                        {name: this.tournamentBackupJson.tournament.name});
                    this.$location.path('/my/tournament/' + ok.data);
                },
                (...a) => {
                    this.requestStatus.failed(...a);
                    this.info.clearAll();
                });
    }
}
