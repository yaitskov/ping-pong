import AngularBean from 'core/angular/AngularBean.js';

export default class JudgeMatchListCtrl extends AngularBean {
    static get $inject() {
        return ['Match', 'Tournament', 'Participant', 'mainMenu', '$location',
                'pageCtx', 'requestStatus', '$routeParams', 'cutil', 'binder', '$scope'];
    }

    completeMatch(match) {
        if (this.bid) {
            match.participants = [
                match.enemy,
                {uid: this.bid,
                 name: this.cutil.findValBy(this.bids, {uid: +this.bid}).name}];
        }
        this.pageCtx.put('last-scoring-match', match);
        this.$location.path('/judge/score/set/' + this.tournamentId + '/' + match.mid);
    }

    bidChange() {
        this.matches = null;
        this.pageCtx.put('last-bid', this.bid);
        this.Match.bidMatchesNeedToPlay(
            {tournamentId: this.$routeParams.tournamentId,
             bid: this.bid
            },
            (matches) => {
                this.requestStatus.complete();
                this.matches = matches.matches;
                this.progress = matches.progress;
            },
            (...a) => this.requestStatus.failed(...a));
    }

    loadRunningMatches() {
        this.showTables = true;
        this.orderField = 'table.label';
        this.Match.myMatchesNeedToJudge(
            {tournamentId: this.$routeParams.tournamentId},
            (matches) => {
                this.requestStatus.complete();
                this.matches = matches.matches || [];
                this.progress = matches.progress;
                this.tournamentNotOpen = !matches.length;
            },
            (...a) => this.requestStatus.failed(...a));
    }

    loadParticipantsWithIncompletMatches() {
        this.orderField = 'enemy.name';
        this.showTables = false;
        this.Participant.findByState(
            {tid: this.$routeParams.tournamentId,
             states: ['Wait', 'Play']
            },
            (bids) => {
                this.requestStatus.complete();
                this.bids = bids || [];
                if (bids.length) {
                    this.bid = "" + this.cutil.findValByO(
                        bids, {uid: +this.pageCtx.get('last-bid')}, bids[0]).uid;
                    this.bidChange();
                }
            },
            (...a) => this.requestStatus.failed(...a));
    }

    onFormReady() {
        this.requestStatus.startLoading();
        this.Tournament.parameters(
            {tournamentId: this.$routeParams.tournamentId},
            (rules) => {
                if (rules.place && rules.place.ad == 'g') {
                    this.loadRunningMatches();
                } else {
                    this.loadParticipantsWithIncompletMatches();
                }
            },
            (...a) => this.requestStatus.failed(...a));
    }

    bind() {
        this.binder(this.$scope, {
            'event.main.menu.ready': (e) => this.mainMenu.setTitle('Match Judgement'),
            'event.request.status.ready': (event) => this.onFormReady()
        });
    }

    constructor() {
        super(...arguments);

        this.matches = null;
        this.tournamentId = this.$routeParams.tournamentId;
        this.bid = null;
        this.bids = null;

        this.bind();
    }
}
