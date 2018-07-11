import SimpleController from 'core/angular/SimpleController.js';

export default class TrPlayOffResultCtrl extends SimpleController {
    static get $inject () {
        return ['Tournament', 'Group', 'mainMenu', '$routeParams', 'eBarier',
                'AjaxInfo', 'binder', '$scope', '$rootScope'];
    }

    $onInit() {
        this.ajax = this.AjaxInfo.scope(this.$scope);
        this.matches = null;
        this.winners = null;
        this.tournament = null;
        this.activeGroup = null;
        this.currentCid = null;
        this.tid = this.$routeParams.tournamentId;
        const params = {tournamentId: this.tid};

        const barWidgetsReady = this.eBarier.create(
            ['view', 'category'], (e) => this.loadCategories());

        this.mainMenu.setTitle('PlayOff ladder');
        this.binder(this.$scope, {
            'event.playoff.view.ready': (e) => barWidgetsReady.got('view'),
            'event.category.switch.ready':  (e) => barWidgetsReady.got('category'),
            'event.category.switch.current': (e, cid) => this.pickCategory(cid)
        });
    }

    loadCategories() {
        this.ajax.doAjax('',
                         this.Group.list,
                         {tournamentId: this.tid},
                         (tournament) => {
                             this.allGroups = tournament.groups;
                             this.categories = tournament.categories;
                             this.$rootScope.$broadcast('event.category.switch.data',
                                                        {list: this.categories});
                         });
    }

    pickCategory(cid) {
        this.ajax.doAjax('',
                         this.Tournament.playOffMatches,
                         {tournamentId: this.tid, categoryId: cid},
                         (tournament) => {
                             tournament.tid = this.tid;
                             this.$rootScope.$broadcast('event.playoff.view.data', tournament);
                         });
    }
}
