'use strict';

angular.
    module('openTournament').
    component('openTournament', {
        templateUrl: 'open-tournament/open-tournament.template.html',
        controller: ['Match', 'Tournament', 'mainMenu', '$routeParams',
                     '$timeout', '$q', 'requestStatus',
                     function (Match, Tournament, mainMenu, $routeParams,
                               $timeout, $q, requestStatus) {
                         mainMenu.setTitle('Incomplete games of the tournament');
                         var self = this;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};
                         self.load = function () {
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
                                     mainMenu.setTitle('Matches of ' + self.tournament.name);
                                     $timeout(self.load, 60 * 1000);
                                 },
                                 requestStatus.failed);
                         };
                         self.load();
                     }
                    ]
        });
