import angular from 'angular';
import './tr-management.scss';
import template from './tr-management.template.html';
import whistle from './whistle.png';

angular.
    module('tournament').
    component('tournamentManagement', {
        templateUrl: template,
        controller: ['$routeParams', 'Tournament', 'auth', 'mainMenu', 'eBarier', '$rootScope',
                     '$http', 'pageCtx', 'requestStatus', '$location', 'binder', '$scope',
                     function ($routeParams, Tournament, auth, mainMenu, eBarier, $rootScope,
                               $http, pageCtx, requestStatus, $location, binder, $scope) {
                         var self = this;
                         var setLastTournament = eBarier.create(
                             ['got.tr', 'mm.ready'],
                             (tournament) =>
                                 $rootScope.$broadcast('event.mm.last.tournament',
                                                       {
                                                           tid: tournament.tid,
                                                           name: tournament.name,
                                                           role: 'Admin',
                                                           state: tournament.state
                                                       }));
                         self.whistle = whistle;
                         self.tournament = null;
                         self.wantRemove = false;
                         self.errorHasUncheckedUsers = null;
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/presence/' + $routeParams.tournamentId] = 'CheckPresence';
                         ctxMenu['#!/my/tournament/categories/' + $routeParams.tournamentId] = 'Categories';
                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 mainMenu.setTitle('MyTournament', ctxMenu);
                                 setLastTournament.got('mm.ready');
                             },
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Tournament.aMine(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (tournament) {
                                         requestStatus.complete();
                                         mainMenu.setTitle(['Administration of', {name: tournament.name}], ctxMenu);
                                         setLastTournament.got('got.tr', tournament);
                                         self.tournament = tournament;
                                         pageCtx.put('tournamentInfoForCategories',
                                                     {tid: self.tournament.tid,
                                                      name: self.tournament.name,
                                                      state: self.tournament.state});
                                     },
                                   (...a) => requestStatus.failed(...a));
                             }
                         });
                         this.haveFollowingTournaments = () => self.tournament &&
                             (self.tournament.state == 'Close' ||
                              self.tournament.state == 'Canceled') && !self.tournament.masterTid;
                         this.canBeginDrafting = () => self.tournament &&
                              (self.tournament.state == 'Hidden' ||
                               self.tournament.state == 'Announce');
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
                                   (...a) => requestStatus.failed(...a));
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
                                  bid: participant.bid},
                                 function (ok) {
                                     self.expelAndOpenTournament();
                                 },
                               (...a) => requestStatus.failed(...a));
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
                                         $location.path('/my/matches/judgement/' + self.tournament.tid);
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
                               (...a) => requestStatus.failed(...a));
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
                                   (...a) => requestStatus.failed(...a));
                         };
                     }
                    ]
    });
