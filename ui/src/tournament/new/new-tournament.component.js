import angular from 'angular';
import template from './new-tournament.template.html';

angular.
    module('tournament').
    component('newTournament', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location', 'placePicker', 'pageCtx', '$scope',
                     function (auth, mainMenu, $http, $location, placePicker, pageCtx, $scope) {
                         mainMenu.setTitle('New Tournament');
                         this.tournament = pageCtx.get('newTournament') || {ticketPrice: 30};
                         if (this.tournament.tid) {
                             delete this.tournament.tid;
                         }
                         var self = this;
                         this.dataPickerUi = {};
                         this.dateOpts = {enableTime: false,
                                          disableMobile: true,
                                          dateFormat: 'Y-m-d',
                                          minDate: new Date()};
                         this.place = placePicker.getChosenPlace() || pageCtx.get('place') || {};
                         if (self.place.pid) {
                             self.tournament.placeId = self.place.pid;
                             self.tournament.placeName = self.place.name;
                             pageCtx.put('newTournament', self.tournament);
                         }
                         $scope.$watch('$ctrl.tournament.startTime', function (oldValue, newValue) {
                             // space hack
                             if (self.tournament.startTime) {
                                 self.tournament.startTime = self.tournament.startTime.replace(/([^ ])(AM|PM)$/, '$1 $2');
                             }
                         });
                         this.choosePlace = function () {
                             pageCtx.put('newTournament', self.tournament);
                             placePicker.pickFrom();
                         };
                         this.showTournamentParameters = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             pageCtx.put('newTournament', self.tournament);
                             $location.path('/tournament/new/parameters');
                         };
                     }]});
