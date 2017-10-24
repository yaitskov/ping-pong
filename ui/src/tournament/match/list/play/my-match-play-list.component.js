import angular from 'angular';
import template from './my-match-play-list.template.html';

angular.module('tournament').
    component('myMatchPlayList', {
        templateUrl: template,
        controller: ['Match', 'Tournament', 'mainMenu', '$q', 'cutil', '$routeParams',
                     'pageCtx', 'auth', 'requestStatus', '$location', 'longDateTime',
                     function (Match, Tournament, mainMenu, $q, cutil, $routeParams,
                               pageCtx, auth, requestStatus, $location, longDateTime) {
                         mainMenu.setTitle('My matches to be played');
                         this.matches = null;
                         this.openMatch = null;
                         this.tournament = null;
                         this.previouslyScored = pageCtx.get('last-scoring-match') || {};
                         var self = this;
                         this.matchScoring = function () {
                             pageCtx.put('last-scoring-match', self.openMatch);
                             pageCtx.put('match-max-score-' + self.openMatch.mid, self.openMatch.matchScore);
                             $location.path("/complete/my/match/" + self.openMatch.mid);
                         };
                         this.nextFormatted = function () {
                             if (!self.tournament || !self.tournament.next) {
                                 return {};
                             }
                             return {
                                 url: '#!/tournaments/' + self.tournament.next.tid,
                                 name: self.tournament.next.name,
                                 date: longDateTime(self.tournament.next.opensAt)
                             };
                         };
                         this.prevFormatted = function () {
                             if (!self.tournament || !self.tournament.previous) {
                                 return {};
                             }
                             return {
                                 url: '#!/tournaments/result/' + self.tournament.previous.tid,
                                 name: self.tournament.previous.name
                             };
                         };
                         this.iSawMyOutcomeInTournament = function () {
                             pageCtx.put('last-scoring-match', {});
                             self.previouslyScored = {};
                         };
                         this.checkTournamentEnd = function () {
                             self.previouslyScored.ended = (
                                 self.previouslyScored.tid &&
                                     self.tournament &&
                                     self.tournament.previous &&
                                     self.previouslyScored.tid == self.tournament.previous.tid);
                         };
                         requestStatus.startLoading();
                         $q.all([Tournament.myRecent({}).$promise,
                                 Match.myMatchesNeedToPlay({tournamentId: $routeParams.tournamentId}).$promise]).
                             then(
                                 function (responses) {
                                     self.tournament = responses[0];
                                     self.checkTournamentEnd();
                                     var matches = responses[1];
                                     requestStatus.complete();
                                     self.matches = matches;
                                     self.openMatch = cutil.findValByO(matches, {state: 'Game'});
                                     if (self.openMatch) {
                                         pageCtx.putEnemyUid(auth.myUid(), self.openMatch.mid,
                                                             self.openMatch.enemy.uid);
                                     }
                                 },
                                 requestStatus.failed);
                     }
                    ]
        });
