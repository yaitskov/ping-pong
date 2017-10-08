import angular from 'angular';
import template from './enlist.offline.template.html';

angular.
    module('participant').
    component('enlistOffline', {
        templateUrl: template,
        controller: ['$routeParams', 'Tournament', 'mainMenu', '$q', 'Group',
                     'requestStatus', 'Participant', '$http', 'auth', 'pageCtx',
                     function ($routeParams, Tournament, mainMenu, $q, Group,
                               requestStatus, Participant, $http, auth, pageCtx) {
                         mainMenu.setTitle('Offline enlist');
                         var self = this;
                         self.groupId = null;
                         self.categoryId = null;
                         self.tournamentId = $routeParams.tournamentId;
                         self.categories = null;
                         self.categoryGroups = null;
                         self.rank = 1;
                         self.rankRange = {};
                         self.enlisted = [];
                         self.form = {};
                         requestStatus.startLoading('Loading');
                         self.loadGroupPopulations = function (tid, cid) {
                             requestStatus.startLoading('Loading');
                             self.groupId = null;
                             Group.populations(
                                 {
                                     tournamentId: tid,
                                     categoryId: cid},
                                 function (ok) {
                                     self.categoryGroups = ok;
                                     for (var i in self.categoryGroups.links) {
                                         self.groupId = self.categoryGroups.links[i].gid;
                                             break;
                                     }
                                     requestStatus.complete();
                                 },
                                 requestStatus.failed);
                         };
                         var req = {tournamentId: $routeParams.tournamentId};
                         $q.all([
                             Tournament.aDrafting(req).$promise,
                             Tournament.parameters(req).$promise]).
                             then(
                                 (responses) => {
                                     var tournament = responses[0];
                                     self.tournament = tournament;
                                     self.rules = responses[1];
                                     var rnkOptions = self.rules.casting.providedRankOptions;
                                     if (rnkOptions) {
                                         self.rankRange = {min: rnkOptions.minValue, max: rnkOptions.maxValue};
                                     }
                                     requestStatus.complete();
                                     mainMenu.setTitle(['Offline enlist to', {name: tournament.name}]);
                                     self.categories = tournament.categories;
                                     var wasCategoryId = pageCtx.get('offline-category-' + $routeParams.tournamentId);
                                     for (var i in tournament.categories) {
                                         if (!wasCategoryId || wasCategoryId == tournament.categories[i].cid) {
                                             self.categoryId = tournament.categories[i].cid;
                                             break;
                                         }
                                     }
                                     if (!self.categoryId) {
                                         for (var i in tournament.categories) {
                                             self.categoryId = tournament.categories[i].cid;
                                             break;
                                         }
                                     }
                                     if (self.tournament.state == 'Open' && self.categoryId) {
                                         self.loadGroupPopulations($routeParams.tournamentId, self.categoryId);
                                     }
                                 },
                                 requestStatus.failed);

                         this.activate = function (cid) {
                             self.categoryId = cid;
                             self.loadGroupPopulations($routeParams.tournamentId, self.categoryId);
                             pageCtx.put('offline-category-' + $routeParams.tournamentId, self.categoryId);
                         };
                         this.activateGroup = function (gid) {
                             self.groupId = gid;
                             pageCtx.put('offline-group-' + $routeParams.tournamentId, self.groupId);
                         };

                         this.enlist = function (bidState) {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Enlisting', self.tournament);
                             if (!self.categoryId) {
                                 requestStatus.validationFailed("CategoryNotChosen");
                                 return;
                             }
                             var name = self.firstName + ' ' + self.lastName;
                             var req = {tid: self.tournamentId,
                                        cid: self.categoryId,
                                        bidState: bidState,
                                        name: name
                                       };
                             if (self.rules.casting.providedRankOptions) {
                                 req.providedRank = self.rank;
                             }
                             if (self.groupId) {
                                 req.groupId = self.groupId;
                             }
                             $http.post('/api/tournament/enlist-offline',
                                        req,
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (resp) {
                                         requestStatus.complete();
                                         self.firstName = '';
                                         self.lastName = '';
                                         self.enlisted.unshift({uid: resp.data, name: name});
                                         self.form.$setPristine(true);
                                         jQuery(self.form.firstName.$$element).focus();
                                     },
                                     requestStatus.failed);
                         };
                     }
                    ]
    });
