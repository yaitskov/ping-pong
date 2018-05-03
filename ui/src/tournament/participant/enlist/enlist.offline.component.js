import angular from 'angular';
import './enlist.scss';
import template from './enlist.offline.template.html';

angular.
    module('participant').
    component('enlistOffline', {
        templateUrl: template,
        controller: ['$routeParams', 'Tournament', 'mainMenu', '$q', 'Group',
                     'requestStatus', 'Participant', '$http', 'auth', 'pageCtx',
                     'binder', '$scope',
                     function ($routeParams, Tournament, mainMenu, $q, Group,
                               requestStatus, Participant, $http, auth, pageCtx,
                               binder, $scope) {
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
                         self.loadGroupPopulations = function (tid, cid) {
                             requestStatus.startLoading('Loading');
                             Group.populations(
                                 {tournamentId: tid, categoryId: cid},
                                 function (ok) {
                                     self.categoryGroups = ok;
                                     if (self.groupId === 0) {
                                         for (let glink of self.categoryGroups.links) {
                                             self.groupId = Math.max(glink.gid, self.groupId);
                                         }
                                     } else {
                                         for (let glink of self.categoryGroups.links) {
                                             self.groupId = glink.gid;
                                             break;
                                         }
                                     }
                                     requestStatus.complete();
                                 },
                                 requestStatus.failed);
                         };
                         var req = {tournamentId: $routeParams.tournamentId};
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Offline enlist'),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading('Loading');
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
                             }
                         });

                         this.activate = function (cid) {
                             self.categoryId = cid;
                             self.loadGroupPopulations($routeParams.tournamentId, self.categoryId);
                             pageCtx.put('offline-category-' + $routeParams.tournamentId, self.categoryId);
                         };
                         this.activateGroup = function (gid) {
                             self.groupId = gid;
                             if (gid) {
                                pageCtx.put('offline-group-' + $routeParams.tournamentId, self.groupId);
                             }
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
                             var req = {tid: self.tournamentId,
                                        cid: self.categoryId,
                                        bidState: bidState,
                                        name: self.fullName
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
                                         self.enlisted.unshift({uid: resp.data,
                                                                name: self.fullName});
                                         self.participantFullName = '';
                                         self.form.$setPristine(true);
                                         jQuery(self.form.fullName.$$element).focus();
                                         if (self.groupId === 0) {
                                              self.loadGroupPopulations($routeParams.tournamentId, self.categoryId);
                                         } else {
                                              self.categoryGroups.populations[
                                                    self.categoryGroups.links.findIndex((link) => link.gid == self.groupId)] += 1;
                                         }
                                     },
                                     requestStatus.failed);
                         };
                     }
                    ]
    });
