import angular from 'angular';
import '../../list.scss';
import template from './rank-manually.template.html';

angular.module('participant').
    component('rankBidManually', {
        templateUrl: template,
        controller: ['Casting', 'Tournament', '$q', 'mainMenu', 'requestStatus',
                     '$routeParams', 'shuffleArray', '$scope', 'binder',
                     function (Casting, Tournament, $q, mainMenu, requestStatus,
                               $routeParams, shuffleArray, $scope, binder) {
                         var self = this;
                         self.sortMode = 'manual';
                         self.saveOrder = () => {
                             requestStatus.startLoading('saving');
                             Casting.orderBidsManually(
                                 {tid: $routeParams.tournamentId,
                                  cid: $routeParams.categoryId,
                                  uids: self.rankedBids.map((bid) => bid.user.bid)},
                                 (ok) => {
                                     requestStatus.complete(ok);
                                     window.history.back();
                                 },
                               (...a) => requestStatus.failed(...a));
                         };
                         $scope.$watch('$ctrl.sortMode', (newVavlue, oldValue) => {
                             if (!self.rankedBids) {
                                 return;
                             }
                             if (self.sortMode == 'random') {
                                 shuffleArray(self.rankedBids);
                             } else if (self.sortMode == 'manual') {
                                 self.rankedBids.sort((a, b) => (a.seed || 0) - (b.seed || 0));
                             } else if (self.sortMode == 'providedRank') {
                                 self.rankedBids.sort((a, b) => (b.providedRank || 0) - (a.providedRank || 0));
                             } else {
                                 throw "Unknown sort mode " + self.sortMode;
                             }
                         });
                         binder($scope, {
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 $q.all([
                                     Tournament.aDrafting(
                                         {tournamentId: $routeParams.tournamentId}).$promise,
                                     Casting.manualBidsOrder(
                                         {tournamentId: $routeParams.tournamentId,
                                          categoryId: $routeParams.categoryId})
                                         .$promise]).then(
                                             (responses) => {
                                                 self.tournament = responses[0];
                                                 self.categoryName = self.tournament.categories.find(
                                                     (c) => c.cid == $routeParams.categoryId).name;
                                                 mainMenu.setTitle(['Ranking category',
                                                                    {name: self.categoryName}]);
                                                 self.rankedBids = responses[1];
                                                 requestStatus.complete(responses);
                                             },
                                             (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]});
