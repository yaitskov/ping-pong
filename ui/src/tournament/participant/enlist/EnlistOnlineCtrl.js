import SimpleController from 'core/angular/SimpleController.js';

export default class EnlistOnlineCtrl extends SimpleController {
    static get $inject () {
        return ['$routeParams', 'Tournament', 'auth', 'mainMenu', 'binder',
                '$scope', '$http', '$location', 'requestStatus', 'cutil',
                'pageCtx', 'eBarier', '$rootScope', 'InfoPopup'];
    }

    $onInit() {
        this.extraCategories = false;
        this.info = this.InfoPopup.createScope();
        this.tournament = {};
        this.activeCategories = 0;
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
        this.activeCategories = this.countActiveCategories();
        var rnkOptions = tournament.rules.casting.pro;
        if (tournament.rules.casting.pro) {
            this.rankRange = {min: rnkOptions.minValue, max: rnkOptions.maxValue};
            this.rank = rnkOptions.minValue;
        }
    }

    countActiveCategories() {
        return Object.values(this.tournament.categoryState || {}).
            filter(s => s != 'Quit').length;
    }

    showActivateExtra() {
        const ac = this.countActiveCategories();
        if (!ac) {
            return false;
        }
        return !this.extraCategories &&
            ac < this.tournament.categories.length &&
            this.tournament.rules.enlist == 'OC';
    }

    categoryBtnClasses(cid) {
        if (!this.tournament.categoryState) {
            return {};
        }
        const state = this.tournament.categoryState[cid] || '';
        if (this.tournament.state == 'Draft') {
            return {'btn-primary': state == 'Want'};
        }
        return {
            'btn-primary': state == 'Play' || state == 'Wait' || state == 'Here',
            'btn-danger':  state == 'Lost' || state == 'Quit' ,
            'btn-warning': state == 'Expl',
            'btn-succes': state.substr(0, 3) == 'Win'
        };
    }

    activateExtra() {
        this.extraCategories = true ^ this.extraCategories;
    }

    showCategoryBtn(cid) {
        const state = (this.tournament.categoryState || {})[cid];
        if (this.tournament.state == 'Draft') {
            switch (this.tournament.rules.enlist) {
            case 'OC':
                return this.extraCategories || !this.activeCategories || state != 'Quit';
            case 'OT':
                return this.activeCategories && state != 'Quit' || !this.activeCategories;
            default:
                throw new Error(`Bad enlist policy ${this.tournament.rules.enlist}`);
            }
        }
        return this.activeCategories == 0 || state;
    }

    toggleEnlistment(cid) {
        const bidState = (this.tournament.categoryState || {})[cid];
        if (this.tournament.state == 'Draft') {
            if (bidState == 'Want') {
                this.showQuitConfirm = cid;
            } else {
                this.enlistMe(cid);
            }
        } else {
            if (this.cutil.has(bidState, ['Paid', 'Here', 'Play', 'Wait'])) {
                this.showQuitConfirm = cid;
            } else {
                this.info.transInfo('cannot resign in terminal state');
            }
        }
    }

    showScheduleLink() {
        return Object.values(this.tournament.categoryState || {}).length;
    }

    showThanksForEnlist() {
        return Object.values(this.tournament.categoryState || {}).
            filter(s => this.cutil.has(s, ['Paid', 'Here', 'Play', 'Wait', 'Want'])).length;
    }

    showResultLink() {
        return this.showScheduleLink();
    }

    showEnlisted() {
        this.$location.path('/tournament/enlisted/' + this.tournament.tid);
    }

    enlistMe(cid) {
        this.showQuitConfirm = false;
        this.requestStatus.startLoading('Enlisting', this.tournament);
        if (this.auth.isAuthenticated()) {
            var req = {tid: this.tournament.tid, categoryId: cid};
            if (this.tournament.rules.casting.pro) {
                req.providedRank = this.rank;
            }
            this.$http.post('/api/tournament/enlist',
                            req, {headers: {session: this.auth.mySession()}}).
                then(
                    (ok) => {
                        this.requestStatus.complete();
                        this.setCategoryState(cid, 'Want');
                        this.info.transInfo('enlisted to category',
                                            {name: this.categoryName(cid)});

                    },
                    (...a) => this.requestStatus.failed(...a));
        } else {
            this.auth.requireLogin();
        }
    }

    setCategoryState(cid, state) {
        this.tournament.categoryState = this.tournament.categoryState || {};
        this.tournament.categoryState[cid] = state;
        this.extraCategories = false;
        this.activeCategories = this.countActiveCategories();
    }

    categoryName(cid) {
        if (!this.tournament.categories)  {
            return '';
        }
        const category = this.cutil.findValByO(this.tournament.categories, {cid: cid});
        if (category) {
            return category.name;
        }
        return '';
    }

    resign(cid) {
        this.showQuitConfirm = 0;
        this.requestStatus.startLoading('Resigning', this.tournament);
        this.Tournament.resign(
            {tid: this.tournament.tid, cid: cid},
            () => {
                this.setCategoryState(cid, 'Quit');
                this.requestStatus.complete();
                this.info.transInfo('resigned from category',
                                    {name: this.categoryName(cid)});
            },
            (...a) => this.requestStatus.failed(...a));
    }
}
