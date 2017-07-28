'use strict';

angular.
    module('myTournament').
    component('myTournament', {
        templateUrl: 'my-tournament/my-tournament.template.html',
        controller: ['$routeParams', 'Tournament', 'auth', 'mainMenu', '$http', 'pageCtx', 'requestStatus', '$location',
                     function ($routeParams, Tournament, auth, mainMenu, $http, pageCtx, requestStatus, $location) {
                         mainMenu.setTitle('My tournament ...');
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/presence/' + $routeParams.tournamentId] = 'Check Presence';
                         ctxMenu['#!/my/tournament/categories/' + $routeParams.tournamentId] = 'Categories';
                         var self = this;
                         self.tournament = null;
                         self.wantRemove = false;
                         requestStatus.startLoading();
                         Tournament.aMine(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 requestStatus.complete();
                                 mainMenu.setTitle('Administration of ' + tournament.name);
                                 mainMenu.setContextMenu(ctxMenu);
                                 self.tournament = tournament;
                                 pageCtx.put('tournamentInfoForCategories',
                                             {tid: self.tournament.tid,
                                              name: self.tournament.name,
                                              state: self.tournament.state});
                             },
                             requestStatus.failed);
                         this.canBeginDrafting = function () {
                             return self.tournament && (self.tournament.state == 'Hidden'
                                                        || self.tournament.state == 'Announce');
                         };
                         this.isDrafting = function () {
                             return self.tournament && self.tournament.state == 'Draft';
                         };
                         this.canEdit = function () {
                             return self.tournament && (self.tournament.state == 'Hidden'
                                                        || self.tournament.state == 'Draft'
                                                        || self.tournament.state == 'Announce');
                         }
                         this.canConfigureParams = function () {
                             return self.tournament && (self.tournament.state == 'Hidden'
                                                        || self.tournament.state == 'Draft'
                                                        || self.tournament.state == 'Announce');
                         };
                         this.beginDrafting = function () {
                             requestStatus.startLoading();
                             $http.post('/api/tournament/state',
                                        {tid: self.tournament.tid, state: 'Draft'},
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         self.tournament.state = 'Draft';
                                         requestStatus.complete();
                                     },
                                     requestStatus.failed);
                         };
                         this.open = function () {
                             console.log("Begin drafting for " + self.tournament.tid);
                             requestStatus.startLoading('Starting the tournament');
                             $http.post('/api/tournament/begin',
                                        self.tournament.tid,
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         self.tournament.state = 'Open';
                                         requestStatus.complete();
                                         $location.path("/my/matches/judgement");
                                     },
                                     requestStatus.failed);
                         };
                         this.isNotCanceled = function () {
                             return self.tournament && !(self.tournament.state == 'Close'
                                                         || self.tournament.state == 'Canceled'
                                                         || self.tournament.state == 'Replaced');
                         };
                         this.cancel = function () {
                             this.wantRemove = true;
                         }
                         this.confirmCancel = function () {
                             this.wantRemove = false;
                             requestStatus.startLoading('Cancelation of the tournament');
                             $http.post("/api/tournament/cancel",
                                        self.tournament.tid,
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         self.tournament.state = 'Canceled';
                                         requestStatus.complete();
                                     },
                                     requestStatus.failed);
                         };
                     }
                    ]
    });
