'use strict';

angular.module('categoryMemberList').
    component('categoryMemberList', {
        templateUrl: 'my-tournament/categories/members/members.template.html',
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus', 'pageCtx', 'Category',
                     function ($http, mainMenu, $routeParams, auth, requestStatus, pageCtx, Category) {
                         mainMenu.setTitle('Category Members');
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         mainMenu.setContextMenu(ctxMenu);
                         this.tournamentId = $routeParams.tournamentId;
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
