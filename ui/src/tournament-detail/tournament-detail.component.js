'use strict';

angular.
    module('tournamentDetail').
    component('tournamentDetail', {
        templateUrl: 'tournament-detail/tournament-detail.template.html',
        cache: false,
        controller: ['$routeParams', 'Tournament', 'auth', 'mainMenu',
                     '$http', '$location', 'requestStatus', 'cutil', 'pageCtx',
                     function ($routeParams, Tournament, auth, mainMenu,
                               $http,  $location, requestStatus, cutil, pageCtx) {
                         mainMenu.setTitle('Drafting...');
                         var self = this;
                         self.myCategory = pageCtx.get('my-category-' + $routeParams.tournamentId) || {};
                         self.tournament = null;
                         this.activate = function (cid) {
                             self.myCategory.cid = cid;
                             pageCtx.put('my-category-' + $routeParams.tournamentId, self.myCategory);
                         };
                         requestStatus.startLoading('Loading');
                         Tournament.aDrafting(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 requestStatus.complete();
                                 mainMenu.setTitle('Drafting to ' + tournament.name);
                                 self.tournament = tournament;
                                 if (self.tournament.myCategoryId) {
                                     self.myCategory = {cid: tournament.myCategoryId,
                                                        name: cutil.findValBy(self.tournament.categories,
                                                                              {cid: tournament.myCategoryId}).name}
                                 }
                             },
                             function (r) {
                                 requestStatus.failed(r, {tid: $routeParams.tournamentId});
                             });
                         this.enlistMe = function () {
                             console.log("Enlist Me");
                             requestStatus.startLoading('Enlisting', self.tournament);
                             if (!self.myCategory.cid) {
                                 self.error = "Choose category";
                                 requestStatus.validationFailed("Category is not chosen. Choose");
                                 return;
                             }
                             if (auth.isAuthenticated()) {
                                 $http.post('/api/tournament/enlist',
                                            {tid: self.tournament.tid,
                                             categoryId: self.myCategory.cid} ,
                                            {headers: {session: auth.mySession()}}).
                                     then(
                                         function (okResp) {
                                             requestStatus.complete();
                                             self.tournament.myCategoryId = self.myCategory.cid;
                                             self.myCategory.name = cutil.findValBy(self.tournament.categories,
                                                                                    {cid: self.myCategory.cid}).name;
                                         },
                                         requestStatus.failed);
                             } else {
                                 auth.requireLogin();
                             }
                         };

                         this.resign = function () {
                             console.log("Resign from tournament " + self.tournament.tid);
                             requestStatus.startLoading('Resigning', self.tournament);
                             $http.post("/api/tournament/resign",
                                        self.tournament.tid,
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (okResp) {
                                         self.tournament.myCategoryId = null;
                                         requestStatus.complete();
                                     },
                                     requestStatus.failed);
                         };
                     }
                    ]
    });
