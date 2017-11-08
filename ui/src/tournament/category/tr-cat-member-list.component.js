import angular from 'angular';
import template from './tr-cat-member-list.template.html';

angular.module('tournamentCategory').
    component('trCategoryMemberList', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus', 'pageCtx', 'Category', 'binder', '$scope',
                     function ($http, mainMenu, $routeParams, auth, requestStatus, pageCtx, Category, binder, $scope) {
                         this.tournamentId = $routeParams.tournamentId;
                         this.categoryId = $routeParams.categoryId;
                         this.newCategoryName = '';
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 var ctxMenu = {};
                                 ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                                 mainMenu.setTitle('Category Members', ctxMenu);
                             },
                             'event.request.status.ready': (e) => {
                                 requestStatus.startLoading('Loading members'),
                                 Category.members(
                                     {tournamentId: self.tournamentId, categoryId: $routeParams.categoryId},
                                     function (catInfo) {
                                         requestStatus.complete();
                                         self.members = catInfo.users;
                                         self.catInfo = catInfo;
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
        });
