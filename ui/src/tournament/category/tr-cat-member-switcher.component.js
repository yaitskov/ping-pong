import angular from 'angular';
import template from './tr-cat-member-switcher.template.html';

angular.module('tournamentCategory').
    component('trCategoryMemberySwitcher', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'binder', '$scope',
                     'requestStatus', 'pageCtx', '$location', 'Category', 'Participant',
                     function ($http, mainMenu, $routeParams, auth, binder, $scope,
                               requestStatus, pageCtx, $location, Category, Participant) {
                         this.tournament = pageCtx.get('tournamentInfoForCategories') || {tid: $routeParams.tournamentId};
                         this.categories = null;
                         var self = this;
                         this.assignCategory = function (cid) {
                             requestStatus.startLoading('Changing category');
                             Participant.setCategory(
                                 {cid: cid,
                                  tid: $routeParams.tournamentId,
                                  uid: $routeParams.participantId},
                                 function (ok) {
                                     history.back();
                                 },
                               (...a) => requestStatus.failed(...a));
                         };

                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 var ctxMenu = {};
                                 ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                                 mainMenu.setTitle('Pick category', ctxMenu);
                             },
                             'event.request.status.ready': (e) => {
                                 requestStatus.startLoading('Load categories');
                                 Category.ofTournament(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (categories) {
                                         requestStatus.complete();
                                         self.categories = categories;
                                     },
                                   (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]
        });
