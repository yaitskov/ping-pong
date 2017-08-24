import angular from 'angular';
import template from './change.template.html';

angular.module('changeCategory').
    component('changeCategory', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth',
                     'requestStatus', 'pageCtx', '$location', 'Category', 'Participant',
                     function ($http, mainMenu, $routeParams, auth,
                               requestStatus, pageCtx, $location, Category, Participant) {
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         mainMenu.setTitle('Pick category', ctxMenu);
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
                                 requestStatus.failed);
                         };
                         requestStatus.startLoading('Load categories');
                         Category.ofTournament(
                             {tournamentId: $routeParams.tournamentId},
                             function (categories) {
                                 requestStatus.complete();
                                 self.categories = categories;
                             },
                             requestStatus.failed);
                     }
                    ]
        });
