import angular from 'angular';
import template from './participant-match-list-to-judge.template.html';

angular.module('tournament').
    component('participantMatchListToJudge', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', 'cutil', '$routeParams',
                     'pageCtx', 'auth', 'requestStatus', '$location', 'binder', '$scope',
                     function (Match, mainMenu, cutil, $routeParams,
                               pageCtx, auth, requestStatus, $location, binder, $scope) {
                         this.tournamentId = $routeParams.tournamentId;
                         this.matches = null;
                         this.openMatch = null;
                         var self = this;
                         this.matchScoring = function (match) {
                             pageCtx.put('last-scoring-match', match);
                             $location.path('/participant/score/set/' + $routeParams.tournamentId + '/' + match.mid);
                         };
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('My matches to be played'),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Match.myMatchesNeedToPlay(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (matches) {
                                         requestStatus.complete();
                                         self.matches = matches;
                                         if (matches.showTables) {
                                             self.openMatch = cutil.findValByO(matches.matches, {state: 'Game'});
                                         }
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
        });
