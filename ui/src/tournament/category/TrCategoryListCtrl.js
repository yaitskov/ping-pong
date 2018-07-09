import SimpleController from 'core/angular/SimpleController.js';

export default class TrCategoryListCtrl extends SimpleController {
    static get $inject () {
        return ['mainMenu', '$routeParams', 'AjaxInfo',
                'pageCtx', '$location', '$scope', 'Category'];
    }

    $onInit() {
        this.ajax = this.AjaxInfo.scope(this.$scope);
        this.tournament = this.pageCtx.get('tournamentInfoForCategories') ||
            {tid: this.$routeParams.tournamentId};
        this.categories = null;
        this.newCategoryName = '';

        const ctxMenu = {};
        ctxMenu['#!/my/tournament/' + this.$routeParams.tournamentId] = 'Tournament';
        this.mainMenu.setTitle('Categories', ctxMenu);

        this.ajax.doAjax(
            'Load categories',
            this.Category.ofTournament,
            {tid: this.$routeParams.tournamentId},
            (cats) =>  this.categories = cats);
    }

    beginDraft() {
        this.ajax.doAjax(
            "Openning draft",
            this.Tournament.state,
            {tid: this.tournament.tid, state: 'Draft'},
            (ok) => {
                this.tournament.state = 'Draft';
                this.pageCtx.put('tournamentInfoForCategories',
                                 {tid: this.tournament.tid,
                                  name: this.tournament.name,
                                  state: this.tournament.state});
                this.$location.path('/my/tournament/' + this.tournament.tid);
            });
    }

    addGroup() {
        this.form.$setSubmitted();
        if (!this.form.$valid) {
            return;
        }
        this.ajax.doPost(
            'Adding category',
            '/api/category/create',
            {tid: this.$routeParams.tournamentId,
             name: this.newCategoryName},
            (cid) => {
                this.categories.push({cid: cid, name: this.newCategoryName});
                this.newCategoryName = '';
                this.form.$setPristine(true);
            });
    }

    removeCategoryByIdx(idx) {
        this.ajax.doAjax(
            'Removing category',
            this.Category.deleteCat,
            {cid: this.categories[idx].cid,
             tid: this.$routeParams.tournamentId
            },
            (ok) => this.categories.splice(idx, 1));
    }
}
