import angular from 'angular';
import template from './review-sets.template.html';

angular.
    module('tournament').
    component('reviewSets', {
        templateUrl: template,
        controller: ['$routeParams', 'binder', '$rootScope', '$scope',
                     function ($routeParams, binder, $rootScope, $scope) {
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;

                         self.strongSet = function (iSet) {
                             return self.strong[iSet];
                         };
                         self.appendSet = function () {
                             $rootScope.$broadcast('event.review.match.set.appended');
                         };
                         self.removeLastSet = function () {
                             $rootScope.$broadcast('event.review.match.set.popped');
                         };
                         self.pickSet = function (setIdx, set) {
                             $rootScope.$broadcast('event.review.match.set.picked', setIdx, set);
                         };
                         self.strong = {};
                         self.config = {edit: false};
                         binder($scope, {
                             'event.review.match.strong.data': (event, strong) => {
                                 self.strong = strong;
                             },
                             'event.review.match.config': (event, config) => {
                                 self.config = config;
                             },
                             'event.review.match.data': (event, match) => {
                                 self.matchReview = match;
                                 self.matchScore = self.matchReview.score;
                                 self.participants = self.matchReview.participants;
                                 let result = [];
                                 if (self.participants && self.participants.length == 2) {
                                     let l = self.matchScore.sets[self.participants[0].uid].length;
                                     for (let i of l) {
                                         result.push({a: self.matchScore.sets[self.participants[0].uid][i],
                                                      b: self.matchScore.sets[self.participants[1].uid][i]});
                                     }
                                 }
                                 self.sets = result;
                             }
                         });
                         $rootScope.$broadcast('event.review.match.ready');
                     }]});
