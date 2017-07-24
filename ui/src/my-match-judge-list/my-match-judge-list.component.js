'use strict';

angular.module('myMatchJudgeList').
    component('myMatchJudgeList', {
        templateUrl: 'my-match-judge-list/my-match-judge-list.template.html',
        controller: ['Match', 'Tournament', 'mainMenu', '$q', '$location', 'pageCtx', 'requestStatus',
                     function (Match, Tournament, mainMenu, $q, $location, pageCtx, requestStatus) {
                         mainMenu.setTitle('Match Judgement');
                         this.matches = null;
                         this.tournament = null;
                         var self = this;
                         this.previouslyScoredEnded = null;
                         this.checkTournamentEnd = function () {
                             self.previouslyScored = (
                                 pageCtx.get('last-admin-scoring-match-id')
                                     && pageCtx.get('last-admin-scoring-match-id') == self.tournament.previous.tid);
                         };
                         this.completeMatch = function (mid, idx) {
                             pageCtx.put('last-admin-scoring-match-id', this.matches[idx].tid);
                             pageCtx.putMatchParticipants(mid, this.matches[idx].participants);
                             $location.path('/complete/match/' + mid);
                         };
                         self.iSawTournamentEnd = function () {
                             pageCtx.put('last-admin-scoring-match-id', null);
                             self.previouslyScored = null;
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
