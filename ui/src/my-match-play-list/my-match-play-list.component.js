'use strict';

angular.module('myMatchPlayList').
    component('myMatchPlayList', {
        templateUrl: 'my-match-play-list/my-match-play-list.template.html',
        controller: ['Match', 'mainMenu',
                     function (Match, mainMenu) {
                         mainMenu.setTitle('Planned matches');
                         this.matches = null;
                         var self = this;
                         self.error = null;
                         Match.myMatchesNeedToPlay(
                             {},
                             function (matches) {
                                 self.error = null;
                                 console.log("Loaded matches " + matches.length);
                                 self.matches = matches;
                             },
                             function (error) {
                                 self.matches = [];
                                 if (error.status == 502) {
                                     self.error = "Server is not available";
                                 } else if (error.status == 401) {
                                     self.error = "Session is not valid. Click link to send an email with authentication link.";
                                 } else if (error.status == 500) {
                                     if (typeof error.data == 'string') {
                                         self.error = "Server error" + (error.data.indexOf('<') < 0 ? '' : ' ' + error.data);
                                     } else if (typeof error.data == 'object') {
                                         self.error = "Server error: " + self.error.message;
                                     } else {
                                         self.error = "Server error";
                                     }
                                 } else {
                                     self.error = "Failed to load matches";
                                 }
                             });
                     }
                    ]
        });
