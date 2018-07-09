import SimpleController from 'core/angular/SimpleController.js';

export default class TournamentListToWatchCtrl extends SimpleController {
    static get $inject () {
        return ['Tournament', 'mainMenu', 'AjaxInfo', '$scope'];
    }

    $onInit() {
        this.mainMenu.setTitle('Running tournaments');
        this.AjaxInfo.scope(this.$scope).doAjax(
            '', this.Tournament.running,
            {alsoCompleteInDays: 3},
            (ts) => this.tournaments = ts);
    }

    percent(tournament) {
        if (!tournament.gamesOverall) {
            return '-';
        }
        var ratio = tournament.gamesComplete / tournament.gamesOverall;
        return Math.round(ratio * 100.0) + '%';
    }
}
