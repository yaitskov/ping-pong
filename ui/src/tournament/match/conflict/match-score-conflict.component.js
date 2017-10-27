import angular from 'angular';
import template from './match-score-conflict.template.html';

angular.
    module('tournament').
    component('matchScoreConflict', {
        templateUrl: template,
        controller: ['pageCtx', '$routeParams',
                     function (pageCtx, $routeParams) {
                         var self = this;
                         self.conflict = pageCtx.get('match-score-conflict-' + $routeParams.matchId);
                         self.matchScore = self.conflict.matchScore;
                         self.yourSet = self.conflict.yourSet;
                         self.yourSetScore = self.conflict.yourSetScore;
                         self.participants = self.participants;
                         var result = [];
                         var l = self.matchScore.sets[self.participants[0].uid].length;
                         for (var i = 0; i < l; ++i) {
                             result.push({a: self.matchScore.sets[self.participants[0].uid][i],
                                          b: self.matchScore.sets[self.participants[1].uid][i]});
                         }
                         this.sets = result;
                         this.isWon = function (set) {
                             return set.a > set.b;
                         };
                     }]});
