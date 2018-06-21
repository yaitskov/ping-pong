import angular from 'angular';
import './enlist.scss';
import template from './enlist.online.template.html';

angular.
    module('participant').
    component('enlistOnline', {
        templateUrl: template,
        controller: ['$routeParams', 'Tournament', 'auth', 'mainMenu', 'binder', '$scope',
                     '$http', '$location', 'requestStatus', 'cutil', 'pageCtx', 'eBarier', '$rootScope',
                     function ($routeParams, Tournament, auth, mainMenu, binder, $scope,
                               $http,  $location, requestStatus, cutil, pageCtx, eBarier, $rootScope) {
                         var self = this;
                         self.myCategory = pageCtx.get('my-category-' + $routeParams.tournamentId) || {};
                         self.tournament = null;
                         self.showQuitConfirm = false;
                         this.activate = function (cid) {
                             self.myCategory.cid = cid;
                             pageCtx.put('my-category-' + $routeParams.tournamentId, self.myCategory);
                         };
                         this.showScheduleLink = function () {
                             return self.tournament &&
                                 cutil.has(self.tournament.bidState, ['Paid', 'Here', 'Play', 'Wait']);
                         };
                         this.showResultLink = function () {
                             return self.tournament &&
                                 cutil.has(self.tournament.bidState, ['Quit', 'Win1', 'Win2', 'Win3', 'Lost', 'Expl']);
                         };
                         this.canResignFutureTournament = function () {
                             return self.tournament &&
                                 cutil.has(self.tournament.bidState,
                                           ['Want', 'Paid', 'Here']);
                         };
                         this.canResignActiveTournament = function () {
                             return self.tournament &&
                                 !self.showQuitConfirm &&
                                 cutil.has(self.tournament.bidState,
                                           ['Play', 'Wait', 'Rest']);
                         };
                         this.showCategoryList = function () {
                             return self.tournament &&
                                 (!self.tournament.bidState ||
                                  self.tournament.bidState == 'Quit');
                         };
                         this.showMyCategory = function () {
                             return self.tournament &&
                                 cutil.has(self.tournament.bidState, ['Want', 'Paid', 'Here', 'Play', 'Wait', 'Rest']);
                         };
                         this.canEnlist = function () {
                             return self.tournament &&
                                 (!self.tournament.bidState || self.tournament.bidState == 'Quit') &&
                                 self.tournament.state == 'Draft' &&
                                 !self.tournament.iamAdmin;
                         };
                         this.ensureResign = function () {
                             self.showQuitConfirm = true;
                         };
                         this.showEnlisted = function () {
                             pageCtx.put('categories', {list: self.tournament.categories,
                                                        currentCid: self.myCategory ? self.myCategory.cid : 0});
                             $location.path('/tournament/enlisted/' + self.tournament.tid);
                         };
                         this.enlistMe = function () {
                             requestStatus.startLoading('Enlisting', self.tournament);
                             if (!self.myCategory.cid) {
                                 requestStatus.validationFailed('CategoryNotChosen');
                                 return;
                             }
                             if (auth.isAuthenticated()) {
                                 var req = {tid: self.tournament.tid,
                                            categoryId: self.myCategory.cid};
                                 if (self.tournament.rules.casting.pro) {
                                     req.providedRank = self.rank;
                                 }
                                 $http.post('/api/tournament/enlist',
                                            req, {headers: {session: auth.mySession()}}).
                                     then(
                                         function (okResp) {
                                             requestStatus.complete();
                                             self.tournament.myCategoryId = self.myCategory.cid;
                                             self.tournament.bidState = 'Want';
                                             self.myCategory.name = cutil.findValBy(self.tournament.categories,
                                                                                    {cid: self.myCategory.cid}).name;
                                         },
                                       (...a) => requestStatus.failed(...a));
                             } else {
                                 auth.requireLogin();
                             }
                         };

                         this.resign = function () {
                             requestStatus.startLoading('Resigning', self.tournament);
                             Tournament.resign(
                                 self.tournament.tid,
                                 function () {
                                     self.tournament.bidState = 'Quit';
                                     requestStatus.complete();
                                 },
                               (...a) => requestStatus.failed(...a));
                         };

                         var setLastTournament = eBarier.create(
                             ['got.tr', 'mm.ready'],
                             (tournament) =>
                                 $rootScope.$broadcast('event.mm.last.tournament',
                                                       {
                                                           tid: tournament.tid,
                                                           name: tournament.name,
                                                           role: tournament.iAmAdmin ? 'Admin' : 'Participant',
                                                           state: tournament.state
                                                       }));

                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 mainMenu.setTitle('Drafting');
                                 setLastTournament.got('mm.ready');
                             },
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading('Loading');
                                 Tournament.aDrafting(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (tournament) {
                                         requestStatus.complete();
                                         mainMenu.setTitle(['Drafting to', {name: tournament.name}]);
                                         setLastTournament.got('got.tr', tournament);
                                         self.tournament = tournament;
                                         var rnkOptions = tournament.rules.casting.pro;
                                         if (tournament.rules.casting.pro) {
                                             self.rankRange = {min: rnkOptions.minValue, max: rnkOptions.maxValue};
                                             self.rank = rnkOptions.minValue;
                                         }
                                         if (self.tournament.myCategoryId) {
                                             self.myCategory = {cid: tournament.myCategoryId,
                                                                name: cutil.findValBy(self.tournament.categories,
                                                                                      {cid: tournament.myCategoryId}).name}
                                         }
                                     },
                                     function (r) {
                                         requestStatus.failed(r, {tid: $routeParams.tournamentId});
                                     });
                             }
                         });
                     }
                    ]
    });
