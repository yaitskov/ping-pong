import SimpleController from 'core/angular/SimpleController.js';

export default class EnlistOfflineCtrl extends SimpleController {
    static get $inject () {
        return ['$routeParams', 'Tournament', 'mainMenu', '$q', 'Group',
                'requestStatus', 'Participant', '$http', 'auth', 'pageCtx',
                'binder', '$scope'];
    }

    $onInit() {
        this.groupId = null;
        this.categoryId = null;
        this.tournamentId = this.$routeParams.tournamentId;
        this.categories = null;
        this.categoryGroups = null;
        this.rank = 1;
        this.rankRange = {};
        this.enlisted = [];
        this.form = {};

        const req = {tournamentId: this.$routeParams.tournamentId};
        this.binder(this.$scope, {
            'event.main.menu.ready': (e) => this.mainMenu.setTitle('Offline enlist'),
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
                            var rnkOptions = this.rules.casting.providedRankOptions;
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
                        this.requestStatus.failed);
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
            this.requestStatus.failed);
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

    enlist(bidState) {
        this.form.$setSubmitted();
        if (!this.form.$valid) {
            return;
        }
        this.requestStatus.startLoading('Enlisting', this.tournament);
        if (!this.categoryId) {
            this.requestStatus.validationFailed("CategoryNotChosen");
            return;
        }
        var req = {tid: this.tournamentId,
                   cid: this.categoryId,
                   bidState: bidState,
                   name: this.fullName
                  };
        if (this.rules.casting.providedRankOptions) {
            req.providedRank = this.rank;
        }
        if (this.groupId) {
            req.groupId = this.groupId;
        }
        this.$http.post('/api/tournament/enlist-offline',
                   req,
                   {headers: {session: this.auth.mySession()}}).
            then(
                (resp) => {
                    this.requestStatus.complete();
                    this.enlisted.unshift({uid: resp.data,
                                           name: this.fullName});
                    this.fullName = '';
                    this.form.$setPristine(true);
                    jQuery(this.form.fullName.$$element).focus();
                    if (this.groupId === 0) {
                        this.loadGroupPopulations(this.$routeParams.tournamentId, this.categoryId);
                    } else {
                        this.categoryGroups.populations[
                            this.categoryGroups.links.findIndex((link) => link.gid == this.groupId)] += 1;
                    }
                },
                this.requestStatus.failed);
    }
}
