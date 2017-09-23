import angular from 'angular';
import template from './new-tournament-parameters.template.html';

angular.
    module('newTournamentParameters').
    component('newTournamentParameters', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location', 'placePicker',
                     'pageCtx', 'requestStatus', 'moment', 'groupSchedule',
                     '$scope',
                     function (auth, mainMenu, $http, $location, placePicker,
                               pageCtx, requestStatus, $moment, groupSchedule, $scope) {
                         mainMenu.setTitle('Tournament Parameters');
                         var self = this;
                         self.tournament = pageCtx.get('newTournament') || {};
                         this.errors = {};
                         self.tournament.rules = self.tournament.rules || {
                             thirdPlaceMatch: 1,
                             group: {
                                 quits: 2,
                                 maxSize: 8,
                                 schedule: {
                                    size2Schedule: {
                                       2: [0, 1],
                                       3: [0, 2, 0, 1, 1, 2]
                                    }
                                 }
                             },
                             match: {
                                 minGamesToWin: 11,
                                 minAdvanceInGames: 2,
                                 minPossibleGames: 0,
                                 setsToWin: 3
                             }
                         };
                         self.groupScheduleErrors = [];
                         self.groupSchedule = groupSchedule.convertToText(
                              self.tournament.rules.group.schedule.size2Schedule);
                         $scope.$watch('$ctrl.groupSchedule', function (newValue, oldValue) {
                              try {
                                 self.groupScheduleErrors = [];
                                 self.tournament.rules.group.schedule.size2Schedule = groupSchedule.parseText(newValue);
                              } catch (e) {
                                 self.groupScheduleErrors.push(e);
                              }
                         });
                         this.options = {
                             quitsFromGroup: {min: 1, max: 5},
                             maxGroupSize: {min: 2, max: 20},
                             matchScore: {min: 1, max: 100}
                         };
                         self.published = self.tournament.tid;
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
                         this.formatScheduleError = groupSchedule.formatScheduleError;
                         this.createTournament = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid || self.groupScheduleErrors.length) {
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
                                     function (resp) {
                                        if (resp.status == 400 && resp.data.message == 'tournament-rules-are-wrong') {
                                           self.errors = resp.data.field2Errors;
                                        }
                                        requestStatus.failed(resp);
                                     });
                         }
                     }
                    ]});
