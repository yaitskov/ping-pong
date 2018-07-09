import angular from 'angular';
import template from './participant-score-set.template.html';

angular.
    module('tournament').
    component('participantScoreSet', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', 'Match', '$routeParams', '$location',
                     'pageCtx', 'requestStatus', '$scope', '$rootScope', 'binder',
                     function (auth, mainMenu, Match, $routeParams, $location,
                               pageCtx, requestStatus, $scope, $rootScope, binder) {
                         this.match = pageCtx.get('last-scoring-match');
                         this.match.participants = [this.match.enemy,
                                                    {bid: this.match.bid, name: '*you*'}];
                         var self = this;
                         self.showConflict = function (conflict) {
                             pageCtx.put('match-score-conflict-' + $routeParams.matchId, conflict);
                             $location.path('/match/user-conflict-review/' + self.match.tid + '/' + $routeParams.matchId);
                         };
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Match Scoring'),
                             'event.match.set.ready': (event) => $rootScope.$broadcast('event.match.set', self.match),
                             'event.match.score.conflict': (event, conflict) => self.showConflict(conflict),
                             'event.match.scored': (event, matchScore) => {
                                 pageCtx.put('match-score-review-' + $routeParams.matchId,
                                             {score: matchScore,
                                              participants: self.match.participants
                                             });
                                 $location.path('/review/user-scored-match/' + self.match.tid + '/' + $routeParams.matchId);
                             }
                         });
                     }]});
