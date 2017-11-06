import angular from 'angular';
import template from './tr-cat-member-list.template.html';

angular.module('tournamentCategory').
    component('trCategoryMemberList', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus', 'pageCtx', 'Category',
                     function ($http, mainMenu, $routeParams, auth, requestStatus, pageCtx, Category) {
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         mainMenu.setTitle('Category Members', ctxMenu);
                         this.tournamentId = $routeParams.tournamentId;
                         this.categoryId = $routeParams.categoryId;
                         this.members = null;
                         this.newCategoryName = '';
                         var self = this;
                         requestStatus.startLoading('Loading members');
                         Category.members(
                             {categoryId: $routeParams.categoryId},
                             function (members) {
                                 requestStatus.complete();
                                 self.members = members;
                             },
                             requestStatus.failed);
                     }
                    ]
        });
