import SimpleController from 'core/angular/SimpleController.js';

export default class ParticipantMatchListToJudgeCtrl extends SimpleController {
    static get $inject () {
        return ['Match', 'mainMenu', 'cutil', '$routeParams', 'AjaxInfo',
                'pageCtx', '$location', '$scope', 'Category'];
    }

    $onInit() {
        this.tournamentId = this.$routeParams.tournamentId;
        this.matches = null;
        this.openMatch = null;
        this.mainMenu.setTitle('My matches to be played'),
        this.ajax = this.AjaxInfo.scope(this.$scope);
        this.ajax.doAjax(
            '',
            this.Match.myMatchesNeedToPlay,
            {tournamentId: this.$routeParams.tournamentId},
            (matches) => {
                this.matches = matches;
                const keys = Object.keys(matches.bidState);
                if (keys.length > 1) {
                    this.ajax.doAjax(
                        'Loading categories',
                        this.Category.ofTournament,
                        {tid: this.tournamentId},
                        (categories) => {
                            this.categories = categories;
                        });
                } else {
                    this.categories = [{cid: keys[0]}];
                }
                if (matches.showTables) {
                    this.openMatch = this.cutil.findValByO(
                        matches.matches, {state: 'Game'});
                }
            });
    }

    matchScoring(match) {
        this.pageCtx.put('last-scoring-match', match);
        this.$location.path(
            `/participant/score/set/${this.$routeParams.tournamentId}/${match.mid}`);
    }
}
