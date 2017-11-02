import angular from 'angular';
import template from './watch-tournament.template.html';

angular.
    module('tournament').
    component('watchTournament', {
        templateUrl: template,
        controller: ['Match', 'Tournament', 'mainMenu', '$routeParams',
                     'refresher', '$q', 'requestStatus', '$scope',
                     function (Match, Tournament, mainMenu, $routeParams,
                               refresher, $q, requestStatus, $scope) {
                         var self = this;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};
                         mainMenu.setTitle('IncompleteTournamentGames');
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
                                     mainMenu.setTitle(['MatchesOf', {name: self.tournament.name}]);
                                 },
                                 requestStatus.failed);
                         });
                     }
                    ]
        });
