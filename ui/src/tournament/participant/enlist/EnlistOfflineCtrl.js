import SimpleController from 'core/angular/SimpleController.js';

export default class EnlistOfflineCtrl extends SimpleController {
    static get $inject () {
        return ['$routeParams', 'Tournament', 'mainMenu', '$q', 'Group',
                'requestStatus', 'Participant', '$http', 'auth', 'pageCtx',
                'binder', '$scope', 'WarmUp', 'Suggestion'];
    }

    $onInit() {
        this.enlistPath = '/api/tournament/enlist-offline';
        this.registerOfflinePath = '/api/anonymous/offline-user/register';
        this.WarmUp.warmUp(this.enlistPath);
        this.groupId = null;
        this.categoryId = null;
        this.tournamentId = this.$routeParams.tournamentId;
        this.categories = null;
        this.categoryGroups = null;
        this.rank = 1;
        this.rankRange = {};
        this.enlisted = [];
        this.suggestions = [];
        this.form = {};

        this.mainMenu.setTitle('Offline enlist');

        this.suggestionFiredVersion = 1;
        this.suggestionCompleteVersion = 1;
        this.negativeCache = new Set();

        this.$scope.$watch('$ctrl.fullName', (newV, oldV) => {
            newV = (newV || "").trim().toLowerCase();
            if (!newV) {
                return;
            }
            if (this.negativeCache.has(newV)) {
                return;
            }
            if (oldV && newV.length > oldV.length && this.suggestions.length == 0) {
                return;
            }
            let v = ++this.suggestionVersion;
            this.Suggestion.suggestions(
                {pattern: newV,
                 page: {size: 100, page: 0}},
                (suggestions) => {
                    if (!suggestions.length) {
                        this.negativeCache.add(newV);
                    }
                    if (this.suggestionCompleteVersion > v) {
                        console.log(`drop suggestion version ${v}`);
                        return;
                    }
                    this.suggestionCompleteVersion = v;
                    this.suggestions = suggestions;
                },
                (...a) => this.requestStatus.failed(...a));
        });
        const req = {tournamentId: this.$routeParams.tournamentId};
        this.binder(this.$scope, {
            'event.request.status.ready': (event) => {
                this.requestStatus.startLoading('Loading');
                this.$q.all([
                    this.Tournament.aDrafting(req).$promise,
                    this.Tournament.parameters(req).$promise]).
                    then(
                        (responses) => {
                            const tournament = responses[0];
                            this.tournament = tournament;
                            this.rules = responses[1];
                            var rnkOptions = this.rules.casting.pro;
                            if (rnkOptions) {
                                this.rankRange = {min: rnkOptions.minValue, max: rnkOptions.maxValue};
                            }
                            this.requestStatus.complete();
                            this.mainMenu.setTitle(['Offline enlist to', {name: tournament.name}]);
                            this.categories = tournament.categories;
                            var wasCategoryId = this.pageCtx.get('offline-category-' + this.$routeParams.tournamentId);
                            for (var i in tournament.categories) {
                                if (!wasCategoryId || wasCategoryId == tournament.categories[i].cid) {
                                    this.categoryId = tournament.categories[i].cid;
                                    break;
                                }
                            }
                            if (!this.categoryId) {
                                for (var i in tournament.categories) {
                                    this.categoryId = tournament.categories[i].cid;
                                    break;
                                }
                            }
                            if (this.tournament.state == 'Open' && this.categoryId) {
                                this.loadGroupPopulations(this.$routeParams.tournamentId, this.categoryId);
                            }
                        },
                        (...a) => this.requestStatus.failed(...a));
            }
        });
    }

    loadGroupPopulations(tid, cid) {
        this.requestStatus.startLoading('Loading');
        this.Group.populations(
            {tournamentId: tid, categoryId: cid},
            (ok) => {
                this.categoryGroups = ok;
                if (this.groupId === 0) {
                    for (let glink of this.categoryGroups.links) {
                        this.groupId = Math.max(glink.gid, this.groupId);
                    }
                } else {
                    for (let glink of this.categoryGroups.links) {
                        this.groupId = glink.gid;
                        break;
                    }
                }
                this.requestStatus.complete();
            },
            (...a) => this.requestStatus.failed(...a));
    }

    activate(cid) {
        this.categoryId = cid;
        this.loadGroupPopulations(this.$routeParams.tournamentId, this.categoryId);
        this.pageCtx.put('offline-category-' + this.$routeParams.tournamentId, this.categoryId);
    };

    activateGroup(gid) {
        this.groupId = gid;
        if (gid) {
            this.pageCtx.put('offline-group-' + this.$routeParams.tournamentId, this.groupId);
        }
    }

    register() {
        this.form.$setSubmitted();
        if (!this.form.$valid) {
            return;
        }
        this.requestStatus.startLoading('User registration...');
        this.$http.post(this.registerOfflinePath, {name: this.fullName},
                        {headers: {session: this.auth.mySession()}}).
            then(
                (resp) => {
                    this.requestStatus.complete();
                    this.form.$setPristine(true);
                    jQuery(this.form.fullName.$$element).focus();
                    this.enlist(resp.data, this.fullName);
                },
                (...a) => this.requestStatus.failed(...a));
    }

    findBidState() {
        switch (this.tournament.state) {
        case 'Draft':
            return 'Here';
        case 'Open':
            return 'Wait';
        default:
            throw Error(`bad tournament state ${this.tournament.state}`);
        }
    }

    enlist(uid, name) {
        this.fullName = '';
        this.suggestions = [];

        this.requestStatus.startLoading('Enlisting', this.tournament);
        if (!this.categoryId) {
            this.requestStatus.validationFailed("CategoryNotChosen");
            return;
        }
        var req = {tid: this.tournamentId,
                   cid: this.categoryId,
                   bidState: this.findBidState(),
                   uid: uid
                  };
        if (this.rules.casting.pro) {
            req.providedRank = this.rank;
        }
        if (this.groupId) {
            req.groupId = this.groupId;
        }
        this.$http.post(this.enlistPath, req,
                        {headers: {session: this.auth.mySession()}}).
            then(
                (resp) => {
                    this.requestStatus.complete();
                    this.enlisted.unshift({bid: resp.data,
                                           name: name});
                    if (this.groupId === 0) {
                        this.loadGroupPopulations(this.$routeParams.tournamentId, this.categoryId);
                    } else if (this.groupId) {
                        this.categoryGroups.populations[
                            this.categoryGroups.links.findIndex((link) => link.gid == this.groupId)] += 1;
                    }
                },
                (...a) => this.requestStatus.failed(...a));
    }
}
