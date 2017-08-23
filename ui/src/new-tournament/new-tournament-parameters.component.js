import angular from 'angular';
import template from './new-tournament-parameters.template.html';

angular.
    module('newTournamentParameters').
    component('newTournamentParameters', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location', 'placePicker',
                     'pageCtx', 'requestStatus', 'moment',
                     function (auth, mainMenu, $http, $location, placePicker,
                               pageCtx, requestStatus, $moment) {
                         mainMenu.setTitle('Tournament Parameters');
                         this.tournament = pageCtx.get('newTournament') || {quitsFromGroup: 2,
                                                                            maxGroupSize: 8,
                                                                            matchScore: 3,
                                                                            thirdPlaceMatch: 1};
                         this.options = {
                             quitsFromGroup: {min: 1, max: 5},
                             maxGroupSize: {min: 2, max: 20},
                             matchScore: {min: 1, max: 100}
                         };
                         this.published = this.tournament.tid;
                         var self = this;
                         this.setThirdPlaceMatch = function (v) {
                             self.tournament.thirdPlaceMatch = v;
                             pageCtx.put('newTournament', self.tournament);
                         };
                         this.back = function () {
                             pageCtx.put('newTournament', self.tournament);
                             window.history.back();
                         };
                         this.cancel = function () {
                             pageCtx.put('newTournament', null);
                             $location.path('/tournaments');
                         };
                         this.createTournament = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             if (self.tournament.maxGroupSize <= self.tournament.quitsFromGroup) {
                                 requestStatus.validationFailed('group-size-less-quits');
                                 return;
                             }
                             requestStatus.startLoading('Publishing');
                             var req = angular.copy(self.tournament);
                             req.opensAt =  $moment(req.openDate + " " + req.startTime, 'Y-MM-DD HH:mm A').
                                 utc().format("Y-MM-DDTHH:mm:ss.SSS") + "Z";
                             delete req.placeName;
                             delete req.openDate;
                             delete req.startTime;
                             $http.post('/api/tournament/create',
                                        req,
                                        {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}
                                        }).
                                 then(
                                     function (okResp) {
                                         self.tournament.tid = okResp.data;
                                         self.published = self.tournament.tid;
                                         pageCtx.put('newTournament', self.tournament);
                                         pageCtx.put('tournamentInfoForCategories',
                                                     {tid: self.tournament.tid,
                                                      name: self.tournament.name,
                                                      state: 'Hidden'});
                                         requestStatus.complete();
                                     },
                                     requestStatus.failed);
                         }
                     }
                    ]});
