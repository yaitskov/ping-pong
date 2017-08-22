import angular from 'angular';
import template from './tournament-detail.template.html';

angular.
    module('tournamentDetail').
    component('tournamentDetail', {
        templateUrl: template,
        cache: false,
        controller: ['$routeParams', 'Tournament', 'auth', 'mainMenu',
                     '$http', '$location', 'requestStatus', 'cutil', 'pageCtx', '$translate',
                     function ($routeParams, Tournament, auth, mainMenu,
                               $http,  $location, requestStatus, cutil, pageCtx, $translate) {
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
                                 cutil.has(self.tournament.bidState, ['Paid', 'Here', 'Play']);
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
                         this.enlistMe = function () {
                             $translate(['Enlisting', "ChooseCategory", "CategoryNotChosen"]).then(
                                 function (translations) {
                                     requestStatus.startLoading(translations.Enlisting, self.tournament);
                                     if (!self.myCategory.cid) {
                                         self.error = translations.ChooseCategory;
                                         requestStatus.validationFailed(translations.CategoryNotChosen);
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
                                                     self.tournament.bidState = 'Want';
                                                     self.myCategory.name = cutil.findValBy(self.tournament.categories,
                                                                                            {cid: self.myCategory.cid}).name;
                                                 },
                                                 requestStatus.failed);
                                     } else {
                                         auth.requireLogin();
                                     }
                                 });
                         };

                         this.resign = function () {
                             $translate('Resigning').then(function (resignin) {
                                 requestStatus.startLoading(resignin, self.tournament);
                                 Tournament.resign(
                                     self.tournament.tid,
                                     function () {
                                         self.tournament.bidState = 'Quit';
                                         requestStatus.complete();
                                     },
                                     requestStatus.failed);
                             });
                         };

                         $translate(['Loading', 'Drafting']).then(function (translations) {
                             requestStatus.startLoading(translations.Loading);
                             mainMenu.setTitle(translations.Drafting);
                             Tournament.aDrafting(
                                 {tournamentId: $routeParams.tournamentId},
                                 function (tournament) {
                                     requestStatus.complete();
                                     $translate('Drafting to', {name: tournament.name}).then(
                                         function (title) {
                                             mainMenu.setTitle(title);
                                             self.tournament = tournament;
                                             if (self.tournament.myCategoryId) {
                                                 self.myCategory = {cid: tournament.myCategoryId,
                                                                    name: cutil.findValBy(self.tournament.categories,
                                                                                          {cid: tournament.myCategoryId}).name}
                                             }
                                         });
                                 },
                                 function (r) {
                                     requestStatus.failed(r, {tid: $routeParams.tournamentId});
                                 });
                         });
                     }
                    ]
    });
