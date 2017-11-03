import angular from 'angular';
import template from './review-sets.template.html';

angular.
    module('tournament').
    component('reviewSets', {
        templateUrl: template,
        controller: ['$routeParams', 'pageCtx', 'binder', '$rootScope', '$scope',
                     function ($routeParams, pageCtx, binder, $rootScope, $scope) {
                         var self = this;
                         binder($scope, {
                             'event.review.match.data': (event, match) => {
                                 self.matchReview = match;
                                 self.matchScore = self.matchReview.score;
                                 self.participants = self.matchReview.participants;
                                 var result = [];
                                 var l = self.matchScore.sets[self.participants[0].uid].length;
                                 for (var i = 0; i < l; ++i) {
                                     result.push({a: self.matchScore.sets[self.participants[0].uid][i],
                                                  b: self.matchScore.sets[self.participants[1].uid][i]});
                                 }
                                 self.sets = result;
                             }
                         });
                         $rootScope.$broadcast('event.review.match.ready');
                     }]});
