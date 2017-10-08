import angular from 'angular';
import template from './review-match-score.template.html';

angular.
    module('reviewMatchScore').
    component('reviewMatchScore', {
        templateUrl: template,
        controller: ['mainMenu', 'pageCtx', '$routeParams', 'Match', 'requestStatus',
                     function (mainMenu, pageCtx, $routeParams, Match, requestStatus) {
                         var self = this;
                         mainMenu.setTitle('Match Review');
                         self.conflict =
                         self.matchScore = pageCtx.get('match-score-review-' + $routeParams.matchId);
                         self.participants = pageCtx.getMatchParticipants($routeParams.matchId);
                         var result = [];
                         var l = self.matchScore.sets[self.participants[0].uid].length;
                         for (var i = 0; i < l; ++i) {
                             result.push({a: self.matchScore.sets[self.participants[0].uid][i],
                             b: self.matchScore.sets[self.participants[1].uid][i]});
                         }
                         this.sets = result;
                     }]});
