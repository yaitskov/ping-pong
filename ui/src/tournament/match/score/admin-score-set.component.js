import angular from 'angular';
import template from './admin-score-set.template.html';

angular.
    module('tournament').
    component('adminScoreSet', {
        templateUrl: template,
        controller: ['mainMenu', 'Match', '$routeParams', 'pageCtx', 'requestStatus', '$scope', '$location', 'syncTranslate',
                     function (mainMenu, Match, $routeParams, pageCtx, requestStatus, $scope, $location, syncTranslate) {
                         mainMenu.setTitle('Match Scoring');
                         this.match = pageCtx.get('last-scoring-match');
                         var self = this;
                         self.showConflict = function (conflict) {
                             pageCtx.put('match-score-conflict-' + $routeParams.matchId, conflict);
                             $location.path('/match/admin-conflict-review/' + self.match.tid + '/' + $routeParams.matchId);
                         };
                         binder($scope, {
                             'event.match.score.conflict': (event, conflict) => self.showConflict(conflict),
                             'event.match.scored': (event, matchScore) => {
                                 pageCtx.put('match-score-review-' + $routeParams.matchId,
                                             {score: okResp.matchScore,
                                              participants: self.match.participants
                                             });
                                 $location.path('/review/admin-scored-match/' + self.match.tid + '/' + $routeParams.matchId);
                             }
                         });
                         lateEvent(() => $rootScope.broadcast('event.match.set', self.match));
                     }]});
