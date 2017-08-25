import angular from 'angular';
import template from './my-tournament.template.html';
import whistle from './whistle.png';

angular.
    module('myTournament').
    component('myTournament', {
        templateUrl: template,
        controller: ['$routeParams', 'Tournament', 'auth', 'mainMenu',
                     '$http', 'pageCtx', 'requestStatus', '$location',
                     function ($routeParams, Tournament, auth, mainMenu,
                               $http, pageCtx, requestStatus, $location) {
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/presence/' + $routeParams.tournamentId] = 'CheckPresence';
                         ctxMenu['#!/my/tournament/categories/' + $routeParams.tournamentId] = 'Categories';
                         mainMenu.setTitle('MyTournament', ctxMenu);
                         var self = this;
                         self.whistle = whistle;
                         self.tournament = null;
                         self.wantRemove = false;
                         self.errorHasUncheckedUsers = null;
                         requestStatus.startLoading();
                         Tournament.aMine(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 requestStatus.complete();
                                 mainMenu.setTitle(['Administration of', {name: tournament.name}], ctxMenu);
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
                         this.expelAndOpenTournament = function () {
                             if (!self.errorHasUncheckedUsers || !self.errorHasUncheckedUsers.length) {
                                 requestStatus.complete();
                                 self.errorHasUncheckedUsers = null;
                                 self.open();
                                 return;
                             }
                             var participant = self.errorHasUncheckedUsers.shift();
                             requestStatus.startLoading(['ExpellingOf', {name: participant.name}]);
                             Tournament.expel(
                                 {tid: self.tournament.tid,
                                  uid: participant.uid},
                                 function (ok) {
                                     self.expelAndOpenTournament();
                                 },
                                 requestStatus.failed);
                         };
                         this.cancelExpelAll = function () {
                             self.errorHasUncheckedUsers = null;
                         };
                         this.open = function () {
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
                                     function (error) {
                                         if (error.data.error == 'uncheckedUsers') {
                                             self.errorHasUncheckedUsers = error.data.users;
                                         }
                                         requestStatus.failed(error);
                                     });
                         };
                         this.isNotCanceled = function () {
                             return self.tournament && !(self.tournament.state == 'Close'
                                                         || self.tournament.state == 'Canceled'
                                                         || self.tournament.state == 'Replaced');
                         };
                         this.cancel = function () {
                             this.wantRemove = true;
                         }
                         this.restart = function () {
                             requestStatus.startLoading("Reseting");
                             Tournament.state(
                                 {tid: self.tournament.tid,
                                  state: 'Draft'
                                 },
                                 function (ok) {
                                     requestStatus.complete();
                                     self.tournament.state = 'Draft';
                                 },
                                 requestStatus.failed);
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
