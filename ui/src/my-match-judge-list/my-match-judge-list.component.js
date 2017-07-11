'use strict';

angular.module('myMatchJudgeList').
    component('myMatchJudgeList', {
        templateUrl: 'my-match-judge-list/my-match-judge-list.template.html',
        controller: ['Match', 'Tournament', 'mainMenu', '$q', '$location', 'pageCtx',
                     function (Match, Tournament, mainMenu, $q, $location, pageCtx) {
                         mainMenu.setTitle('Match Judgement');
                         this.matches = null;
                         this.tournament = null;
                         this.completeMatch = function (mid, idx) {
                             pageCtx.putMatchParticipants(mid, this.matches[idx].participants);
                             $location.path('/complete/match/' + mid);
                         };
                         var self = this;
                         self.error = null;
                         $q.all([Tournament.myRecentJudgements({}).$promise,
                                 Match.myMatchesNeedToJudge({}).$promise]).
                             then(
                                 function (responses) {
                                     self.tournament = responses[0];
                                     self.matches = responses[1];
                                     self.error = null;
                                 },
                                 function (error) {
                                     self.matches = [];
                                     if (error.status == 502) {
                                         self.error = "Server is not available";
                                     } else if (error.status == 401) {
                                         self.error = "Session is not valid. Click link to send an email with authentication link.";
                                     } else if (error.status == 500) {
                                         if (typeof error.data == 'string') {
                                             self.error = "Server error" + (error.data.indexOf('<') < 0 ? '' : ' ' + error.data);
                                         } else if (typeof error.data == 'object') {
                                             self.error = "Server error: " + self.error.message;
                                         } else {
                                             self.error = "Server error";
                                         }
                                     } else {
                                         self.error = "Failed to load matches";
                                     }
                                 });
                     }
                    ]
        });
