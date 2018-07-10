import SimpleController from 'core/angular/SimpleController.js';

export default class EnlistOfflineCtrl extends SimpleController {
    static get $inject () {
        return ['$routeParams', 'Tournament', 'mainMenu', 'Group',
                'Participant',  'pageCtx', 'AjaxInfo',
                '$scope', 'WarmUp', 'Suggestion'];
    }

    $onInit() {
        this.enlistPath = '/api/tournament/enlist-offline';
        this.registerOfflinePath = '/api/anonymous/offline-user/register';
        this.ajax = this.AjaxInfo.scope(this.$scope);
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

        this.$scope.$watch('$ctrl.fullName', (n, o) => this.fullNameWatcher(n, o));

        this.ajax.doAjax(
            '', this.Tournament.aDrafting,
            {tournamentId: this.$routeParams.tournamentId},
            (t) => this.setTournament(t));
    }

    setTournament(tournament) {
        this.tournament = tournament;
        this.rules = tournament.rules;
        var rnkOptions = this.rules.casting.pro;
        if (rnkOptions) {
            this.rankRange = {min: rnkOptions.minValue, max: rnkOptions.maxValue};
        }
        this.mainMenu.setTitle(['Offline enlist to', {name: tournament.name}]);
        this.categories = tournament.categories;
        var wasCategoryId = this.pageCtx.get('offline-category-' + tournament.tid);
        for (let cat of tournament.categories) {
            if (!wasCategoryId || wasCategoryId == cat.cid) {
                this.categoryId = cat.cid;
                break;
            }
        }
        if (!this.categoryId) {
            for (let cat of tournament.categories) {
                this.categoryId = cat.cid;
                break;
            }
        }
        if (this.tournament.state == 'Open' && this.categoryId) {
            this.loadGroupPopulations(tournament.tid, this.categoryId);
        }
    }

    fullNameWatcher(newV, oldV) {
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
        this.ajax.doAjax(
            '',
            this.Suggestion.suggestions,
            {pattern: newV, page: {size: 100, page: 0}},
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
            });
    }

    loadGroupPopulations(tid, cid) {
        this.ajax.doAjax(
            '', this.Group.populations,
            {tournamentId: tid, categoryId: cid},
            (ok) => {
                this.categoryGroups = ok;
                if (!this.categoryGroups.links) {
                    return;
                }
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
            });
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
        this.ajax.doPost(
            'User registration',
            this.registerOfflinePath,
            {name: this.fullName},
            (uid) => {
                this.form.$setPristine(true);
                jQuery(this.form.fullName.$$element).focus();
                this.enlist(uid, this.fullName);
            });
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

        if (this.noCategoryMsg) {
            this.ajax.scope.removeMessage(this.noCategoryMsg);
            delete this.noCategoryMsg;
        }

        if (!this.categoryId) {
            this.noCategoryMsg = this.ajax.scope.transError("CategoryNotChosen");
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
        this.ajax.doPost(
            '', this.enlistPath, req,
            (bid) => {
                this.enlisted.unshift({bid: bid, name: name});
                if (this.groupId === 0) {
                    this.loadGroupPopulations(this.$routeParams.tournamentId, this.categoryId);
                } else if (this.groupId) {
                    this.categoryGroups.populations[
                        this.categoryGroups.links.findIndex((link) => link.gid == this.groupId)] += 1;
                }
            });
    }
}
