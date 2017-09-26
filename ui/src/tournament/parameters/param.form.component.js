import angular from 'angular';
import template from './param.form.template.html';

const defaultGroupRules = {quits: 1, groupSize: 8, schedule: {size2Schedule: {2: [0,1]}}};
const defaultPlayOffRules = {thirdPlaceMatch: 0, losings: 1};
const defaultProvidedRankOptions = {maxValue: 10000, minValue: 0, label: 'rating'};

angular.
    module('tournament').
    component('tournamentParametersForm', {
        templateUrl: template,
        controller: ['$scope', '$routeParams', 'groupSchedule', '$timeout', '$rootScope', 'binder', 'requestStatus',
                     function ($scope, $routeParams, groupSchedule, $timeout, $rootScope, binder, requestStatus) {
                         this.tournamentId = $routeParams.tournamentId;
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
                             if (self.rules) {
                                 try {
                                     self.groupScheduleErrors = [];
                                     var schedule = groupSchedule.parseText(newValue);
                                     if (schedule) {
                                         self.rules.group.schedule = {size2Schedule: schedule};
                                     } else {
                                         self.rules.group.schedule = null;
                                     }
                                 } catch (e) {
                                    self.groupScheduleErrors.push(e);
                                 }
                             }
                         });
                         self.update = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid || self.groupScheduleErrors.length) {
                                 return;
                             }
                             var rules = Object.assign({}, self.rules);
                             if (!self.useGroup) {
                                 delete rules.group;
                             }
                             if (!self.usePlayOff) {
                                 delete rules.playOff;
                             }
                             if (rules.group && rules.group.groupSize <= rules.quits) {
                                 requestStatus.validationFailed('group-size-less-quits');
                                 return;
                             }
                             rules.casting = Object.assign({}, rules.casting);
                             if (rules.casting.policy != 'ProvidedRating') {
                                 delete rules.casting.providedRankOptions;
                             }
                             rules.casting.splitPolicy == 'BalancedMix';
                             $rootScope.$broadcast('event.tournament.rules.update', rules);
                         };

                         self.onRulesSet = function (rules) {
                             self.useGroup = rules.group;
                             self.usePlayOff = rules.playOff;
                             self.rules = rules;
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
                         };
                         binder($scope, {
                             'event.tournament.rules.errors': (event, errors) => self.errors = errors,
                             'event.tournament.rules.set': (event, rules) => self.onRulesSet(rules)
                         });
                         self.back = function () {
                             $rootScope.$broadcast('event.tournament.rules.back', self.rules);
                         };
                         self.cancel = function () {
                             $rootScope.$broadcast('event.tournament.rules.cancel', self.rules);
                         };
                     }]});