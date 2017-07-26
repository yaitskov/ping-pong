'use strict';

angular.
    module('newTournamentParameters').
    component('newTournamentParameters', {
        templateUrl: 'new-tournament/new-tournament-parameters.template.html',
        controller: ['auth', 'mainMenu', '$http', '$location', 'placePicker', 'pageCtx', 'requestStatus',
                     function (auth, mainMenu, $http, $location, placePicker, pageCtx, requestStatus) {
                         mainMenu.setTitle('Tournament Parameters');
                         this.tournament = pageCtx.get('newTournament') || {quitsFromGroup: 2,
                                                                            maxGroupSize: 8,
                                                                            matchScore: 3};
                         this.options = {
                             quitsFromGroup: {min: 1, max: 5},
                             maxGroupSize: {min: 2, max: 20},
                             matchScore: {min: 1, max: 100}
                         };
                         this.published = this.tournament.tid;
                         var self = this;
                         this.createTournament = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             if (self.tournament.maxGroupSize <= self.tournament.quitsFromGroup) {
                                 requestStatus.validationFailed(
                                     'Max group size is less than number of people quitting group');
                                 return;
                             }
                             requestStatus.startLoading('Publishing');
                             $http.post('/api/tournament/create',
                                        self.tournament,
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (okResp) {
                                         self.tournament.tid = okResp.data;
                                         self.published = self.tournament.tid;
                                         pageCtx.put('newTournament', self.tournament);
                                         requestStatus.complete();
                                     },
                                     requestStatus.failed);
                         }
                     }
                    ]});
