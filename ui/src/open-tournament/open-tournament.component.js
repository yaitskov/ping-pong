'use strict';

angular.
    module('openTournament').
    component('openTournament', {
        templateUrl: 'open-tournament/open-tournament.template.html',
        controller: ['Match', 'Tournament', 'mainMenu', '$routeParams', '$timeout', '$q',
                     function (Match, Tournament, mainMenu, $routeParams, $timeout, $q) {
                         mainMenu.setTitle('Incomplete games of the tournament');
                         var self = this;
                         self.error = null;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};
                         $q.all([
                             Match.listOpenForWatch(params).$promise,
                             Match.winners(params).$promise,
                             Tournament.aMine(params).$promise
                         ]).then(
                             function (responses) {
                                 self.matches = responses[0];
                                 self.winners = response[1];
                                 self.tournament = response[2];
                                 mainMenu.setTitle('Matches of ' + self.tournament.name);
                             },
                             function (error) {
                                 if (error.status == 502) {
                                     self.error = "Server is not available";
                                 } else if (error.status == 400) {
                                     self.error = "Tournament is complete";
                                 } else if (error.status == 500) {
                                     if (typeof error.data == 'string') {
                                         self.error = "Server error" + (error.data.indexOf('<') < 0 ? '' : ' ' + error.data);
                                     } else if (typeof error.data == 'object') {
                                         self.error = "Server error: " + error.data;
                                     } else {
                                         self.error = "Server error";
                                     }
                                 } else {
                                     self.error = "Failed to load tournaments";
                                 }
                             });
                     }
                    ]
        });
