import angular from 'angular';
import template from './my-match-play-list.template.html';

angular.module('tournament').
    component('myMatchPlayList', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', 'cutil', '$routeParams',
                     'pageCtx', 'auth', 'requestStatus', '$location',
                     function (Match, mainMenu, cutil, $routeParams,
                               pageCtx, auth, requestStatus, $location) {
                         mainMenu.setTitle('My matches to be played');
                         this.tournamentId = $routeParams.tournamentId;
                         this.matches = null;
                         this.openMatch = null;
                         var self = this;
                         this.matchScoring = function (match) {
                             pageCtx.put('last-scoring-match', match);
                             $location.path("/complete/my/match/" + match.mid);
                         };
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
                    ]
        });
