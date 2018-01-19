import angular from 'angular';
import '../admin-score.scss';
import template from './match-score-conflict-user.template.html';

angular.
    module('tournament').
    component('matchScoreConflictUser', {
        templateUrl: template,
        controller: ['mainMenu', 'pageCtx', '$routeParams', '$location', 'binder', '$scope',
                     function (mainMenu, pageCtx, $routeParams, $location, binder, $scope) {
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Match scoring conflict')});
                         self.tournamentId = $routeParams.tournamentId;
                         self.matchId = $routeParams.matchId;
                         self.conflict = pageCtx.get('match-score-conflict-' + $routeParams.matchId);
                         self.matchScore = self.conflict.matchScore;

                         self.showReview = function () {
                             pageCtx.put('match-score-review-' + $routeParams.matchId,
                                         {score: self.matchScore,
                                          participants: self.conflict.participants
                                         });
                             $location.path(`/review/user-scored-match/${self.tournamentId}/${self.matchId}`);
                         };

                         self.continuePlayMatch = function () {
                             var match = pageCtx.get('last-scoring-match');
                             for (var k in self.matchScore.sets) {
                                 match.playedSets = self.matchScore.sets[k].length;
                                 break;
                             }
                             pageCtx.put('last-scoring-match', match);
                             $location.path(`/participant/score/set/${self.tournamentId}/${self.matchId}`);
                         }
                     }]});
