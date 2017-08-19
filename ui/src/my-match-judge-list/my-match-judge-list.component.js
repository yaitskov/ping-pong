import angular from 'angular';
import template from './my-match-judge-list.template.html';

angular.module('myMatchJudgeList').
    component('myMatchJudgeList', {
        templateUrl: template,
        controller: ['Match', 'Tournament', 'mainMenu', '$q', '$location', 'pageCtx', 'requestStatus',
                     function (Match, Tournament, mainMenu, $q, $location, pageCtx, requestStatus) {
                         mainMenu.setTitle('Match Judgement');
                         this.matches = null;
                         this.tournament = null;
                         var self = this;
                         this.previouslyScoredEnded = null;
                         this.checkTournamentEnd = function () {
                             self.previouslyScoredEnded = (
                                 pageCtx.get('last-admin-scoring-tournament-id')
                                     && self.tournament
                                     && self.tournament.previous
                                     && pageCtx.get('last-admin-scoring-tournament-id') == self.tournament.previous.tid);
                         };
                         this.completeMatch = function (mid, idx) {
                             pageCtx.put('last-admin-scoring-tournament-id', this.matches[idx].tid);
                             pageCtx.put('match-max-score-' + mid, this.matches[idx].matchScore);
                             pageCtx.putMatchParticipants(mid, this.matches[idx].participants);
                             $location.path('/complete/match/' + mid);
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
