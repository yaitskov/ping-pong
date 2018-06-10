import SimpleController from 'core/angular/SimpleController.js';

export default class NewTournamentCtrl extends SimpleController {
    static get $inject() {
        return ['auth', 'mainMenu', '$http', '$location',
                'placePicker', 'pageCtx', '$scope', 'binder'];
    }

    $onInit() {
        this.binder(this.$scope, {
            'event.main.menu.ready': (e) => this.mainMenu.setTitle('New Tournament')});
        this.tournament = this.pageCtx.get('newTournament') || {ticketPrice: 30, sport: 'Tennis'};
        if (this.tournament.tid) {
            delete this.tournament.tid;
        }
        this.dataPickerUi = {};
        this.dateOpts = {enableTime: false,
                         disableMobile: true,
                         dateFormat: 'Y-m-d',
                         minDate: new Date()};
        this.place = this.placePicker.getChosenPlace() || this.pageCtx.get('place') || {};
        if (this.place.pid) {
            this.tournament.placeId = this.place.pid;
            this.tournament.placeName = this.place.name;
            this.pageCtx.put('newTournament', this.tournament);
        }
        this.$scope.$watch('$ctrl.tournament.startTime', (oldValue, newValue) => {
            // space hack
            if (this.tournament.startTime) {
                this.tournament.startTime = this.tournament.startTime.replace(/([^ ])(AM|PM)$/, '$1 $2');
            }
        });
    }

    choosePlace() {
        this.pageCtx.put('newTournament', this.tournament);
        this.placePicker.pickFrom();
    }

    showTournamentParameters() {
        this.form.$setSubmitted();
        if (!this.form.$valid) {
            return;
        }
        this.pageCtx.put('newTournament', this.tournament);
        this.$location.path('/tournament/new/parameters');
    }
}
