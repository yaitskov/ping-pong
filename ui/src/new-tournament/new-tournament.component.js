'use strict';

angular.
    module('newTournament').
    component('newTournament', {
        templateUrl: 'new-tournament/new-tournament.template.html',
        controller: ['auth', 'mainMenu', '$http', '$location', 'placePicker', 'pageCtx', '$scope',
                     function (auth, mainMenu, $http, $location, placePicker, pageCtx, $scope) {
                         mainMenu.setTitle('New Tournament');
                         this.tournament = pageCtx.get('newTournament') || {quitsFromGroup: 2,
                                                                            maxGroupSize: 8,
                                                                            ticketPrice: 30,
                                                                            matchScore: 3};
                         var self = this;
                         this.dataPickerUi = {};
                         this.dateOpts = {enableTime: false,
                                          disableMobile: true,
                                          dateFormat: 'Y-m-d',
                                          minDate: new Date()};
                         this.place = placePicker.getChosenPlace() || pageCtx.get('place') || {};
                         self.tournament.placeId = self.place.pid;
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
