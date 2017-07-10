'use strict';

angular.
    module('newTournament').
    component('newTournament', {
        templateUrl: 'new-tournament/new-tournament.template.html',
        controller: ['auth', 'mainMenu', '$http', '$location', 'placePicker', 'pageCtx',
                     function (auth, mainMenu, $http, $location, placePicker, pageCtx) {
                         mainMenu.setTitle('New Tournament');
                         this.tournament = pageCtx.get('newTournament') || {quitsFromGroup: 2, maxGroupSize: 8};
                         var self = this;
                         self.error = null;
                         this.dateOpts = {enableTime: true, dateFormat: 'Y-m-d H:i'};
                         this.place = placePicker.getChosenPlace() || pageCtx.get('place') || {};
                         this.choosePlace = function () {
                             pageCtx.put('newTournament', self.tournament);
                             placePicker.pickFrom();
                         };
                         this.createTournament = function () {
                             console.log("create tournament");
                             if (!self.place) {
                                 self.error = "Pick a place for the tournament";
                                 return;
                             }
                             if (!self.tournament.opensAt) {
                                 self.error = "Pick a date and time for the tournament";
                                 return;
                             }
                             self.tournament.opensAt += ':00.000Z'
                             self.tournament.opensAt = self.tournament.opensAt.replace(' ', 'T');
                             if (!self.tournament.name) {
                                 self.error = "Name the tournament";
                                 return;
                             }
                             self.tournament.placeId = self.place.pid;
                             $http.post('/api/tournament/create',
                                        self.tournament,
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (okResp) {
                                         console.log("tournament created: " + okResp.data);
                                         $location.path('/my/tournaments');
                                     },
                                     function (badResp) {
                                         self.error = "" + badResp;
                                     });
                         };
                     }]});
