'use strict';

angular.
    module('openTournament').
    component('openTournament', {
        templateUrl: 'open-tournament/open-tournament.template.html',
        controller: ['Match', 'mainMenu', '$routeParams',
                     function (Match, mainMenu, $routeParams) {
                         mainMenu.setTitle('Incomplete games of the tournament');
                         var self = this;
                         self.error = null;
                         self.matches = null;
                         self.tid = $routeParams.tournamentId;
                         Match.listOpenForWatch(
                             {tournamentId: $routeParams.tournamentId},
                             function (matches) {
                                 self.matches = matches;
                             },
                             function (error) {
                                 self.matches = [];
                                 if (error.status == 502) {
                                     self.error = "Server is not available";
                                 } else if (error.status == 400) {
                                     self.error = "Tournament is complete";
                                 } else if (error.status == 500) {
                                     if (typeof error.data == 'string') {
                                         self.error = "Server error" + (error.data.indexOf('<') < 0 ? '' : ' ' + error.data);
                                     } else if (typeof error.data == 'object') {
                                         self.error = "Server error: " + self.error.message;
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
