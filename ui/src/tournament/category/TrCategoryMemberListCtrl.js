import SimpleController from 'core/angular/SimpleController.js';

export default class TrCategoryMemberListCtrl extends SimpleController {
    static get $inject () {
        return ['mainMenu', '$routeParams', 'pageCtx',
                '$scope', 'AjaxInfo', 'Category'];
    }

    $onInit() {
        this.tournamentId = this.$routeParams.tournamentId;
        this.categoryId = this.$routeParams.categoryId;
        this.newCategoryName = '';

        const ctxMenu = {};
        ctxMenu['#!/my/tournament/' + this.tournamentId] = 'Tournament';
        this.mainMenu.setTitle('Category Members', ctxMenu);

        this.AjaxInfo.scope(this.$scope).doAjax(
            'Loading members',
            this.Category.members,
            {tournamentId: this.tournamentId, categoryId:
             this.$routeParams.categoryId},
            (catInfo) => {
                this.members = catInfo.users;
                this.catInfo = catInfo;
            });
    }
}
