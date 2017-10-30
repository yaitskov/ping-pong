import angular from 'angular';
import template from './review-sets.template.html';

angular.
    module('tournament').
    component('reviewSets', {
        templateUrl: template,
        controller: ['$routeParams', 'pageCtx',
                     function ($routeParams, pageCtx) {
                         var self = this;
                         self.matchReview = pageCtx.get('match-score-review-' + $routeParams.matchId);
                         self.matchScore = self.matchReview.score;
                         self.participants = self.matchReview.participants;
                         var result = [];
                         var l = self.matchScore.sets[self.participants[0].uid].length;
                         for (var i = 0; i < l; ++i) {
                             result.push({a: self.matchScore.sets[self.participants[0].uid][i],
                             b: self.matchScore.sets[self.participants[1].uid][i]});
                         }
                         this.sets = result;
                     }]});
