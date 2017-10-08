import angular from 'angular';
import template from './my-match-judge-list.template.html';

angular.module('myMatchJudgeList').
    component('myMatchJudgeList', {
        templateUrl: template,
        controller: ['Match', 'Tournament', 'mainMenu', '$q', '$location',
                     'pageCtx', 'requestStatus', 'longDateTime',
                     function (Match, Tournament, mainMenu, $q, $location,
                               pageCtx, requestStatus, longDateTime) {
                         mainMenu.setTitle('Match Judgement');
                         this.matches = null;
                         this.tournament = null;
                         var self = this;
                         this.previouslyScoredEnded = null;
                         this.nextFormatted = function () { // why angularjs translate is strict
                             if (!self.tournament || !self.tournament.next) {
                                 return {};
                             }
                             return {
                                 url: '#!/my/tournament/' + self.tournament.next.tid,
                                 name: self.tournament.next.name,
                                 date: longDateTime(self.tournament.next.opensAt)
                             }
                         }
                         this.checkTournamentEnd = function () {
                             self.previouslyScoredEnded = (
                                 pageCtx.get('last-admin-scoring-tournament-id')
                                     && self.tournament
                                     && self.tournament.previous
                                     && pageCtx.get('last-admin-scoring-tournament-id') == self.tournament.previous.tid);
                         };
                         this.completeMatch = function (match) {
                             pageCtx.put('last-admin-scoring-tournament-id', match.tid);
                             pageCtx.put('match-max-score-' + match.mid, match.matchScore);
                             pageCtx.putMatchParticipants(match.mid, match.participants);
                             $location.path('/complete/match/' + match.mid);
                         };
                         self.iSawTournamentEnd = function () {
                             pageCtx.put('last-admin-scoring-tournament-id', null);
                             self.previouslyScoredEnded = null;
                         };
                         requestStatus.startLoading();
                         $q.all([Tournament.myRecentJudgements({}).$promise,
                                 Match.myMatchesNeedToJudge({}).$promise]).
                             then(
                                 function (responses) {
                                     requestStatus.complete();
                                     self.tournament = responses[0];
                                     self.matches = responses[1];
                                     self.checkTournamentEnd();
                                 },
                                 requestStatus.failed);
                     }
                    ]
        });
