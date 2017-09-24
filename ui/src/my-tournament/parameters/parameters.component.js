import angular from 'angular';
import template from './parameters.template.html';


const defaultGroupRules = {quits: 1, maxSize: 8, schedule: {size2Schedule: {2: [0,1]}}};
const defaultPlayOffRules = {thirdPlaceMatch: 0, losings: 1};
const defaultProvidedRankOptions = {maxValue: 10000, minValue: 0, label: 'rating'};

angular.
    module('tournamentParameters').
    component('tournamentParameters', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location',
                     'pageCtx', '$scope', 'Tournament', 'requestStatus', '$routeParams',
                     'groupSchedule', '$timeout',
                     function (auth, mainMenu, $http, $location,
                               pageCtx, $scope, Tournament, requestStatus, $routeParams, groupSchedule, $timeout) {
                         mainMenu.setTitle('Tournament Modification');
                         this.tournament = {tid: $routeParams.tournamentId};
                         this.groupScheduleErrors = [];
                         this.options = {
                             advance: {min: 1, max: 1000},
                             score: {min: 1, max: 1000},
                             rank: {min: 0, max: 1000000},
                             maxGroupSize: {min: 2, max: 20},
                             sets: {min: 1, max: 1000}
                         };
                         var self = this;
                         self.errors = {};
                         self.formatScheduleError = groupSchedule.formatScheduleError;
                         self.useGroup = false;
                         self.usePlayOff = false;
                         self.scrollBottom = function () {
                             $timeout(function () {
                                 window.scrollTo(0, document.body.scrollHeight);
                             }, 100);
                         };
                         $scope.$watch('$ctrl.groupSchedule', function (newValue, oldValue) {
                             if (self.tournament.rules) {
                                 try {
                                     self.groupScheduleErrors = [];
                                     var schedule = groupSchedule.parseText(newValue);
                                     if (schedule) {
                                         self.tournament.rules.group.schedule = {size2Schedule: schedule};
                                     } else {
                                         self.tournament.rules.group.schedule = null;
                                     }
                                 } catch (e) {
                                    self.groupScheduleErrors.push(e);
                                    console.log("error " + e.template);
                                 }
                             }
                         });
                         this.update = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid || self.groupScheduleErrors.length) {
                                 return;
                             }
                             requestStatus.startLoading('Saving changes');
                             var rules = Object.assign({}, self.tournament.rules);
                             if (!self.useGroup) {
                                 delete rules.group;
                             }
                             if (!self.usePlayOff) {
                                 delete rules.playOff;
                             }
                             rules.casting = Object.assign({}, rules.casting);
                             if (rules.casting.policy != 'ProvidedRating') {
                                 delete rules.casting.providedRankOptions;
                             }
                             rules.casting.splitPolicy == 'BalancedMix';
                             Tournament.updateParams(
                                 {tid: $routeParams.tournamentId, rules: rules},
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
                                 self.useGroup = rules.group;
                                 self.usePlayOff = rules.playOff;
                                 self.tournament.rules = rules = rules.toJSON();
                                 rules.group = rules.group || defaultGroupRules;
                                 rules.playOff = rules.playOff || defaultPlayOffRules;
                                 rules.casting.providedRankOptions = (rules.casting.providedRankOptions
                                                                      || defaultProvidedRankOptions);
                                 rules.casting.policy = 'ProvidedRating';
                                 if (!rules.group.schedule || !rules.group.schedule.size2Schedule) {
                                     rules.group.schedule = defaultGroupRules.schedule;
                                 }
                                 self.groupSchedule = groupSchedule.convertToText(
                                     rules.group.schedule.size2Schedule);
                             },
                             requestStatus.failed);
                     }]});
