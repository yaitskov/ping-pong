'use strict';

angular.module('openTournamentList').
    component('openTournamentList', {
        templateUrl: 'open-tournament-list/open-tournament-list.template.html',
        controller: ['Tournament', 'mainMenu',
                     function (Tournament, mainMenu) {
                         mainMenu.setTitle('Running tournaments');
                         var self = this;
                         self.tournaments = null;
                         Tournament.running(
                             {},
                             function (tournaments) {
                                 self.tournaments = tournaments;
                             },
                             function (error) {
                                 self.tournaments = [];
                                 if (error.status == 502) {
                                     self.error = "Server is not available";
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
