import angular from 'angular';
import template from './participant-score-set.template.html';

angular.
    module('tournament').
    component('participantScoreSet', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', 'Match', '$routeParams',
                     'pageCtx', 'requestStatus', '$scope', '$rootScope', 'binder', 'lateEvent',
                     function (auth, mainMenu, Match, $routeParams,
                               pageCtx, requestStatus, $scope, $rootScope, binder, lateEvent) {
                         mainMenu.setTitle('Match Scoring');
                         this.match = pageCtx.get('last-scoring-match');
                         this.match.participants = [this.match.enemy,
                                                    {uid: auth.myUid(), name: '*you*'}];
                         var self = this;
                         self.showConflict = function (conflict) {
                             pageCtx.put('match-score-conflict-' + $routeParams.matchId, conflict);
                             $location.path('/match/user-conflict-review/' + self.match.tid + '/' + $routeParams.matchId);
                         };
                         binder($scope, {
                             'event.match.score.conflict': (event, conflict) => self.showConflict(conflict),
                             'event.match.scored': (event, matchScore) => {
                                 pageCtx.put('match-score-review-' + $routeParams.matchId,
                                             {score: okResp.matchScore,
                                              participants: self.match.participants
                                             });
                                 $location.path('/review/user-scored-match/' + self.match.tid + '/' + $routeParams.matchId);
                             }
                         });
                         lateEvent(() => $rootScope.broadcast('event.match.set', self.match));
                     }]});
