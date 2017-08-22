import angular from 'angular';
import template from './open-tournament.template.html';

angular.
    module('openTournament').
    component('openTournament', {
        templateUrl: template,
        controller: ['Match', 'Tournament', 'mainMenu', '$routeParams',
                     'refresher', '$q', 'requestStatus', '$scope', '$translate',
                     function (Match, Tournament, mainMenu, $routeParams,
                               refresher, $q, requestStatus, $scope, $translate) {
                         var self = this;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};
                         $translate('IncompleteTournamentGames').then(function (IncompleteTournamentGames) {
                             mainMenu.setTitle(IncompleteTournamentGames);
                             refresher.seconds($scope, 60 * 1000, function () {
                                 requestStatus.startLoading();
                                 $q.all([
                                     Match.listOpenForWatch(params).$promise,
                                     Match.winners(params).$promise,
                                     Tournament.aMine(params).$promise
                                 ]).then(
                                     function (responses) {
                                         requestStatus.complete();
                                         self.matches = responses[0];
                                         self.winners = responses[1];
                                         self.tournament = responses[2];
                                         $translate('MatchesOf', {name: self.tournament.name}).then(function (msg) {
                                             mainMenu.setTitle(msg);
                                         });
                                     },
                                     requestStatus.failed);
                             });
                         });
                     }
                    ]
        });
