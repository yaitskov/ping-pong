import angular from 'angular';
import 'css/medal.scss';
import '../open-tournament.scss';
import template from './watch-tournament.template.html';

angular.
    module('tournament').
    component('watchTournament', {
        templateUrl: template,
        controller: ['Match', 'Tournament', 'mainMenu', '$routeParams',
                     'refresher', '$q', 'requestStatus', '$scope', 'binder',
                     function (Match, Tournament, mainMenu, $routeParams,
                               refresher, $q, requestStatus, $scope, binder) {
                         var self = this;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('IncompleteTournamentGames'),
                             'event.request.status.ready': (event) => {
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
                                             mainMenu.setTitle(
                                                 ['MatchesOf', {name: self.tournament.name}]);
                                         },
                                         requestStatus.failed);
                                 })
                             }
                         });
                     }
                    ]
        });
