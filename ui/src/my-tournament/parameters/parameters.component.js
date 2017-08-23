import angular from 'angular';
import template from './parameters.template.html';

angular.
    module('tournamentParameters').
    component('tournamentParameters', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location', '$translate',
                     'pageCtx', '$scope', 'Tournament', 'requestStatus', '$routeParams',
                     function (auth, mainMenu, $http, $location, $translate,
                               pageCtx, $scope, Tournament, requestStatus, $routeParams) {
                         $translate('Tournament Modification').then(function (msg) {
                             mainMenu.setTitle(msg);
                         });
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
                             $translate('Saving changes').then(function (msg) {
                                 requestStatus.startLoading(msg);
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
                             });
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
