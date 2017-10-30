import angular from 'angular';
import template from './match-score-conflict-user.template.html';

angular.
    module('tournament').
    component('matchScoreConflictUser', {
        templateUrl: template,
        controller: ['mainMenu', 'pageCtx', '$routeParams', '$location',
                     function (mainMenu, pageCtx, $routeParams, $location) {
                         var self = this;
                         mainMenu.setTitle('Match scoring conflict');
                         self.tournamentId = $routeParams.tournamentId;
                         self.matchId = $routeParams.matchId;
                         self.conflict = pageCtx.get('match-score-conflict-' + $routeParams.matchId);
                         self.matchScore = self.conflict.matchScore;

                         self.continuePlayMatch = function () {
                             var match = pageCtx.get('last-scoring-match');
                             for (var k in self.matchScore.sets) {
                                 match.playedSets = self.matchScore.sets[k].length;
                                 break;
                             }
                             pageCtx.put('last-scoring-match', match);
                             $location.path('/participant/score/set/' + self.matchId);
                         }
                     }]});