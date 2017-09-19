import angular from 'angular';
import template from './parameters.template.html';

angular.
    module('tournamentParameters').
    component('tournamentParameters', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location',
                     'pageCtx', '$scope', 'Tournament', 'requestStatus', '$routeParams',
                     'groupSchedule',
                     function (auth, mainMenu, $http, $location,
                               pageCtx, $scope, Tournament, requestStatus, $routeParams, groupSchedule) {
                         mainMenu.setTitle('Tournament Modification');
                         this.tournament = {tid: $routeParams.tournamentId};
                         this.groupScheduleErrors = [];
                         this.options = {
                             quitsGroup: {min: 1, max: 5},
                             maxGroupSize: {min: 2, max: 20},
                             matchScore: {min: 1, max: 100}
                         };
                         var self = this;
                         self.errors = {};
                         self.formatScheduleError = groupSchedule.formatScheduleError;
                         $scope.$watch('$ctrl.groupSchedule', function (newValue, oldValue) {
                             if (self.tournament.rules) {
                                 try {
                                    self.groupScheduleErrors = [];
                                    self.tournament.rules.group.schedule = {size2Schedule: groupSchedule.parseText(newValue)};
                                 } catch (e) {
                                    self.groupScheduleErrors.push(e);
                                    console.log("error " + e.template);
                                 }
                             }
                         });
                         this.setThirdPlaceMatch = function (v) {
                             self.tournament.rules.thirdPlaceMatch = v;
                         };
                         this.update = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid || self.groupScheduleErrors.length) {
                                 return;
                             }
                             requestStatus.startLoading('Saving changes');
                             Tournament.updateParams(
                                 {tid: $routeParams.tournamentId,
                                  rules: self.tournament.rules
                                 },
                                 function (ok) {
                                     requestStatus.complete();
                                     history.back();
                                 },
                                 function (resp) {
                                     if (resp.status == 400 && resp.data.message == 'tournament-rules-are-wrong') {
                                         self.errors = resp.data.field2Errors;
                                     }
                                     requestStatus.failed(resp);
                                 });
                         };
                         requestStatus.startLoading();
                         Tournament.parameters(
                             {tournamentId: $routeParams.tournamentId},
                             function (rules) {
                                 requestStatus.complete();
                                 self.tournament.rules = rules;
                                 if (rules.group.schedule) {
                                    self.groupSchedule = groupSchedule.convertToText(
                                        rules.group.schedule.size2Schedule);
                                 } else {
                                    self.groupSchedule = '';
                                 }
                             },
                             requestStatus.failed);
                     }]});
