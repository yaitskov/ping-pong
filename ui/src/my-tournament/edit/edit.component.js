import angular from 'angular';
import template from './edit.template.html';

angular.
    module('tournamentEdit').
    component('tournamentEdit', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location', 'placePicker',
                     'pageCtx', '$scope', 'Tournament', 'requestStatus', '$routeParams', 'moment',
                     function (auth, mainMenu, $http, $location, placePicker,
                               pageCtx, $scope, Tournament, requestStatus, $routeParams, moment) {
                         mainMenu.setTitle('Tournament Modification');
                         var changes = pageCtx.get('editableTournament');
                         if (changes && $routeParams.tournamentId != changes.tid) {
                             changes = {}
                             pageCtx.put('editableTournament', changes);
                         }
                         this.tournament = Object.assign({}, changes || {});
                         var self = this;
                         this.dataPickerUi = {};
                         this.place = {};
                         this.priceOptions = {min: 0, max: 1000, step: 2, stepIntervalDelay: 1000};
                         this.dateOpts = {enableTime: false,
                                          disableMobile: true,
                                          dateFormat: 'Y-m-d',
                                          minDate: new Date()};

                         $scope.$watch('$ctrl.tournament.startTime', function (oldValue, newValue) {
                             // space hack
                             if (self.tournament.startTime) {
                                 self.tournament.startTime = self.tournament.startTime.replace(/([^ ])(AM|PM)$/, '$1 $2');
                             }
                         });
                         this.splitDate = function () {
                             self.tournament.openDate = moment(self.opensAt).format('Y-MM-DD');
                             self.tournament.startTime = moment(self.opensAt).format('hh:mm a');
                         };
                         this.choosePlace = function () {
                             pageCtx.put('editableTournament', self.tournament);
                             placePicker.pickFrom();
                         };
                         this.update = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             var opensAt = moment(self.tournament.openDate + " "
                                                    + self.tournament.startTime, 'Y-MM-DD HH:mm A').
                                 utc().format("Y-MM-DDTHH:mm:ss.SSS") + "Z";
                             requestStatus.startLoading('Saving changes');
                             Tournament.update(
                                 {tid: self.tournament.tid,
                                  opensAt: opensAt,
                                  price: self.tournament.price,
                                  placeId: self.place.pid,
                                  name: self.tournament.name
                                 },
                                 function (ok) {
                                     placePicker.reset();
                                     pageCtx.put('editableTournament', {});
                                     requestStatus.complete();
                                     history.back();
                                 },
                                 requestStatus.failed);
                         };
                         requestStatus.startLoading();
                         Tournament.aMine(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 requestStatus.complete();
                                 self.place = placePicker.getChosenPlace() || tournament.place;
                                 self.tournament = Object.assign(tournament, pageCtx.get('editableTournament') || {});
                                 self.splitDate();
                                 pageCtx.put('editableTournament', self.tournament);
                             },
                             requestStatus.failed);
                     }]});
