'use strict';

angular.
    module('newTournament').
    component('newTournament', {
        templateUrl: 'new-tournament/new-tournament.template.html',
        controller: ['auth', 'mainMenu', '$http', '$location', 'placePicker', 'pageCtx',
                     function (auth, mainMenu, $http, $location, placePicker, pageCtx) {
                         mainMenu.setTitle('New Tournament');
                         this.tournament = pageCtx.get('newTournament') || {quitsFromGroup: 2,
                                                                            maxGroupSize: 8,
                                                                            matchScore: 3};
                         var self = this;
                         this.dateOpts = {enableTime: false,
                                          disableMobile: true,
                                          dateFormat: 'Y-m-d',
                                          minDate: new Date()};
                         this.place = placePicker.getChosenPlace() || pageCtx.get('place') || {};
                         self.tournament.placeId = self.place.pid;
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
