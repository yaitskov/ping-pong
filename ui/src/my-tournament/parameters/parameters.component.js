'use strict';

angular.
    module('tournamentParameters').
    component('tournamentParameters', {
        templateUrl: 'my-tournament/parameters/parameters.template.html',
        controller: ['auth', 'mainMenu', '$http', '$location',
                     'pageCtx', '$scope', 'Tournament', 'requestStatus', '$routeParams',
                     function (auth, mainMenu, $http, $location,
                               pageCtx, $scope, Tournament, requestStatus, $routeParams) {
                         mainMenu.setTitle('Tournament Modification');
                         this.tournament = {};
                         this.options = {
                             quitsGroup: {min: 1, max: 5},
                             maxGroupSize: {min: 2, max: 20},
                             matchScore: {min: 1, max: 100}
                         };
                         var self = this;
                         this.setThirdPlaceMatch = function (v) {
                             self.tournament.thirdPlaceMatch = v;
                         };
                         this.update = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             requestStatus.startLoading('Saving changes');
                             Tournament.updateParams(
                                 {tid: $routeParams.tournamentId,
                                  quitsGroup: self.tournament.quitsGroup,
                                  maxGroupSize: self.tournament.maxGroupSize,
                                  matchScore: self.tournament.matchScore,
                                  thirdPlaceMatch: self.tournament.thirdPlaceMatch
                                 },
                                 function (ok) {
                                     requestStatus.complete();
                                     history.back();
                                 },
                                 requestStatus.failed);
                         };
                         requestStatus.startLoading();
                         Tournament.parameters(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 requestStatus.complete();
                                 self.tournament = tournament;
                             },
                             requestStatus.failed);
                     }]});
