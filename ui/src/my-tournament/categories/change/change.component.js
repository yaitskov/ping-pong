'use strict';

angular.module('changeCategory').
    component('changeCategory', {
        templateUrl: 'my-tournament/categories/change/change.template.html',
        controller: ['$http', 'mainMenu', '$routeParams', 'auth',
                     'requestStatus', 'pageCtx', '$location', 'Category', 'Participant',
                     function ($http, mainMenu, $routeParams, auth,
                               requestStatus, pageCtx, $location, Category, Participant) {
                         mainMenu.setTitle('Pick category');
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         mainMenu.setContextMenu(ctxMenu);
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
