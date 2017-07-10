'use strict';

angular.
    module('openTournament').
    component('openTournament', {
        templateUrl: 'open-tournament/open-tournament.template.html',
        controller: ['$routeParams', 'Tournament', 'auth', 'mainMenu', '$http', 'cutil', '$location',
                     function ($routeParams, Tournament, auth, mainMenu, $http, cutil, $location) {
                         mainMenu.setTitle('Drafting...');
                         var self = this;
                         self.categoryId = null;
                         self.categoryName = '';
                         self.error = null;
                         self.tournament = null;
                         Tournament.aDrafting(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                     console.log("why? " + tournament);
                                     mainMenu.setTitle('Drafting to ' + tournament.name);
                                     self.tournament = tournament;
                                     if (self.tournament.iamEnlisted) {
                                         self.categoryName = cutil.findValBy(self.tournament.categories,
                                                                             {cid: self.categoryId}).name;
                                     }
                             },
                             function (bo) {
                                 if (bo.status == 404) {
                                     self.error = "Tournament " + $routeParams.tournamentId + " doesn't exist";
                                 } else if (bo.status == 400) {
                                     if (bo.data.error == 'BadState') {
                                         if (bo.data.state == 'Open') {
                                             self.error = "Tournament already began.";
                                         } else if (bo.data.state == 'Close') {
                                             self.error = "Tournament complete.";
                                         } else if (bo.data.state == 'Replaced') {
                                             self.error = "Tournament is replaced.";
                                         } else if (bo.data.state == 'Canceled') {
                                             self.error = "Tournament is canceled.";
                                         } else if (bo.data.state == 'Hidden') {
                                             self.error = "Tournament is not public.";
                                         } else if (bo.data.state == 'Announce') {
                                             self.error = "Tournament is just announced and drating is not begun yet.";
                                         } else {
                                             self.error = "Unkown state " + bo.data.state;
                                         }
                                     } else {
                                         self.error = "Wrong state";
                                     }
                                 } else if (bo.status == 502) {
                                     self.error = "Server is not available";
                                 } else {
                                     self.error = "Failed to get tournament " + $routeParams.tournamentId;
                                 }
                             });
                         this.enlistMe = function () {
                             console.log("Enlist Me");
                             self.error = '';
                             if (!self.categoryId) {
                                 self.error = "Choose category";
                                 return;
                             }
                             if (auth.isAuthenticated()) {
                                 $http.post('/api/tournament/enlist',
                                            {tid: self.tournament.tid,
                                             categoryId: self.categoryId} ,
                                            {headers: {session: auth.mySession()}}).
                                     then(
                                         function (okResp) {
                                             self.tournament.iamEnlisted = true;
                                             self.categoryName = cutil.findValBy(self.tournament.categories,
                                                                                 {cid: self.categoryId}).name;
                                             console.log("enlisted ok " + okResp);
                                         },
                                         function (badResp) {
                                             self.error = "" + badResp;
                                             console.log("failed " + badResp);
                                         });
                             } else {
                                 auth.requireLogin();
                             }
                         };

                         this.resign = function () {
                             console.log("Resign from tournament " + self.tournament.tid);
                             $http.post("/api/tournament/resign",
                                        self.tournament.tid,
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         self.tournament.iamEnlisted = false;
                                         self.categoryName = null;
                                         self.categoryId = null;
                                     },
                                     function (badResp) {
                                         self.error = "failed to resign " + badResp;
                                         console.log("failed to resign " + badResp);
                                     });
                         };
                     }
                    ]
    });
