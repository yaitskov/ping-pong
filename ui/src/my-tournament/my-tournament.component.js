'use strict';

angular.
    module('myTournament').
    component('myTournament', {
        templateUrl: 'my-tournament/my-tournament.template.html',
        controller: ['$routeParams', 'Tournament', 'auth', 'mainMenu', '$http',
                     function ($routeParams, Tournament, auth, mainMenu, $http) {
                         mainMenu.setTitle('My tournament...');
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/presence/' + $routeParams.tournamentId] = 'Check Presence';
                         ctxMenu['#!/my/tournament/categories/' + $routeParams.tournamentId] = 'Categories';
                         var self = this;
                         self.error = null;
                         self.tournament = null;
                         Tournament.aMine(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 mainMenu.setTitle('Administration of ' + tournament.name);
                                 mainMenu.setContextMenu(ctxMenu);
                                 self.tournament = tournament;
                             });
                         this.canBeginDrafting = function () {
                             return self.tournament && (self.tournament.state == 'Hidden'
                                                        || self.tournament.state == 'Announce');
                         };
                         this.isDrafting = function () {
                             return self.tournament && self.tournament.state == 'Draft';
                         };
                         this.beginDrafting = function () {
                             $http.post('/api/tournament/state',
                                        {tid: self.tournament.tid, state: 'Draft'},
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         self.tournament.state = 'Draft';
                                         console.log("tournament began " + okResp);
                                     },
                                     function (badResp) {
                                         self.error = "" + badResp;
                                         console.log("failed to begin " + badResp);
                                     });
                         };
                         this.open = function () {
                             console.log("Begin drafting for " + self.tournament.tid);
                             self.error = '';
                             $http.post('/api/tournament/begin',
                                        self.tournament.tid,
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         self.tournament.state = 'Open';
                                         console.log("tournament began " + okResp);
                                     },
                                     function (badResp) {
                                         self.error = "" + badResp;
                                         console.log("failed to begin " + badResp);
                                     });
                         };
                         this.isNotCanceled = function () {
                             return self.tournament && !(self.tournament.state == 'Close'
                                                         || self.tournament.state == 'Canceled'
                                                         || self.tournament.state == 'Replaced');
                         };
                         this.cancel = function () {
                             console.log("Cancel " + self.tournament.tid);
                             $http.post("/api/tournament/cancel",
                                        self.tournament.tid,
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         self.tournament.state = 'Canceled';
                                     },
                                     function (badResp) {
                                         self.error = "failed to cancel " + badResp;
                                         console.log("failed to cancel " + badResp);
                                     });
                         };
                     }
                    ]
    });
