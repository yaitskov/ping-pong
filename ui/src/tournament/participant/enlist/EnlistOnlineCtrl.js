import SimpleController from 'core/angular/SimpleController.js';

export default class EnlistOnlineCtrl extends SimpleController {
    static get $inject () {
        return ['$routeParams', 'Tournament', 'auth', 'mainMenu', 'binder',
                '$scope', '$http', '$location', 'requestStatus', 'cutil',
                'pageCtx', 'eBarier', '$rootScope'];
    }

    $onInit() {
        this.myCategory = this.pageCtx.get('my-category-' + this.$routeParams.tournamentId) || {};
        this.tournament = null;
        this.showQuitConfirm = false;

        const setLastTournament = this.eBarier.create(
            ['got.tr', 'mm.ready'],
            (tournament) =>
                this.$rootScope.$broadcast('event.mm.last.tournament',
                                      {
                                          tid: tournament.tid,
                                          name: tournament.name,
                                          role: tournament.iAmAdmin ? 'Admin' : 'Participant',
                                          state: tournament.state
                                      }));

        this.binder(this.$scope, {
            'event.main.menu.ready': (e) => {
                this.mainMenu.setTitle('Drafting');
                setLastTournament.got('mm.ready');
            },
            'event.request.status.ready': (event) => {
                this.requestStatus.startLoading('Loading');
                this.Tournament.aDrafting(
                    {tournamentId: this.$routeParams.tournamentId},
                    (tournament) => this.onTournamentReady(tournament, setLastTournament),
                    (...r) => this.requestStatus.failed(r, {tid: this.$routeParams.tournamentId}));
            }
        });
    }

    onTournamentReady(tournament, setLastTournament)  {
        this.requestStatus.complete();
        this.mainMenu.setTitle(['Drafting to', {name: tournament.name}]);
        setLastTournament.got('got.tr', tournament);
        this.tournament = tournament;
        var rnkOptions = tournament.rules.casting.pro;
        if (tournament.rules.casting.pro) {
            this.rankRange = {min: rnkOptions.minValue, max: rnkOptions.maxValue};
            this.rank = rnkOptions.minValue;
        }
        if (this.tournament.myCategoryId) {
            this.myCategory = {cid: tournament.myCategoryId,
                               name: this.cutil.findValBy(this.tournament.categories,
                                                          {cid: tournament.myCategoryId}).name};
        }
    }

    activate(cid) {
        this.myCategory.cid = cid;
        this.pageCtx.put('my-category-' + this.$routeParams.tournamentId, this.myCategory);
    }

    showScheduleLink() {
        return this.tournament &&
            this.cutil.has(this.tournament.bidState, ['Paid', 'Here', 'Play', 'Wait']);
    }

    showResultLink() {
        return this.tournament &&
            this.cutil.has(this.tournament.bidState, ['Quit', 'Win1', 'Win2', 'Win3', 'Lost', 'Expl']);
    }

    canResignFutureTournament() {
        return this.tournament &&
            this.cutil.has(this.tournament.bidState,
                           ['Want', 'Paid', 'Here']);
    }

    canResignActiveTournament() {
        return this.tournament &&
            !this.showQuitConfirm &&
            this.cutil.has(this.tournament.bidState,
                      ['Play', 'Wait', 'Rest']);
    }

    showCategoryList() {
        return this.tournament &&
            (!this.tournament.bidState ||
             this.tournament.bidState == 'Quit');
    }

    showMyCategory() {
        return this.tournament &&
            this.cutil.has(this.tournament.bidState,
                           ['Want', 'Paid', 'Here', 'Play', 'Wait', 'Rest']);
    }

    canEnlist() {
        return this.tournament &&
            (!this.tournament.bidState || this.tournament.bidState == 'Quit') &&
            this.tournament.state == 'Draft' &&
            !this.tournament.iamAdmin;
    };

    ensureResign() {
        this.showQuitConfirm = true;
    }

    showEnlisted() {
        this.pageCtx.put('categories', {list: this.tournament.categories,
                                   currentCid: this.myCategory ? this.myCategory.cid : 0});
        this.$location.path('/tournament/enlisted/' + this.tournament.tid);
    }

    enlistMe() {
        this.requestStatus.startLoading('Enlisting', this.tournament);
        if (!this.myCategory.cid) {
            this.requestStatus.validationFailed('CategoryNotChosen');
            return;
        }
        if (this.auth.isAuthenticated()) {
            var req = {tid: this.tournament.tid,
                       categoryId: this.myCategory.cid};
            if (this.tournament.rules.casting.pro) {
                req.providedRank = this.rank;
            }
            this.$http.post('/api/tournament/enlist',
                            req, {headers: {session: this.auth.mySession()}}).
                then(
                    (okResp) => {
                        this.requestStatus.complete();
                        this.tournament.myCategoryId = this.myCategory.cid;
                        this.tournament.bidState = 'Want';
                        this.myCategory.name = this.cutil.findValBy(this.tournament.categories,
                                                                    {cid: this.myCategory.cid}).name;
                    },
                    (...a) => this.requestStatus.failed(...a));
        } else {
            this.auth.requireLogin();
        }
    }

    resign() {
        this.requestStatus.startLoading('Resigning', this.tournament);
        this.Tournament.resign(
            this.tournament.tid,
            () => {
                this.tournament.bidState = 'Quit';
                this.requestStatus.complete();
            },
            (...a) => this.requestStatus.failed(...a));
    }
}
