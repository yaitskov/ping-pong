'use strict';

angular.module('playInTournamentList').
    component('playInTournamentList', {
        templateUrl: 'play-in-tournament-list/play-in-tournament-list.template.html',
        cache: false,
        controller: ['Tournament', 'mainMenu',
                     function (Tournament, mainMenu) {
                         mainMenu.setTitle('Coming tournaments where I am enlisted');
                         this.tournaments = null;
                         this.error = null;
                         var self = this;
                         Tournament.participateIn(
                             {},
                             function (tournaments) {
                                 self.error = null;
                                 self.tournaments = tournaments;
                             },
                             function (error) {
                                 self.tournaments = [];
                                 if (error.status == 502) {
                                     self.error = "Server is not available";
                                 } else if (error.status == 401) {
                                     self.error = "Session is invalid";
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
