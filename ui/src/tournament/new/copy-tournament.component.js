import angular from 'angular';
import template from './copy-tournament.template.html';

angular.
    module('tournament').
    component('copyTournament', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location', 'pageCtx', '$scope',
                     'Tournament', '$routeParams', 'requestStatus', 'moment',
                     function (auth, mainMenu, $http, $location, pageCtx, $scope,
                               Tournament, $routeParams, requestStatus, $moment) {
                         mainMenu.setTitle('Copy Tournament');
                         this.tournament = pageCtx.get('copyTournament') || {};

                         if (this.tournament.tid) {
                             delete this.tournament.tid;
                         }
                         var self = this;
                         this.dataPickerUi = {};
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
                         this.copyTournament = function () {
                             self.form.$setSubmitted();
                             if (!self.form.$valid) {
                                 return;
                             }
                             pageCtx.put('copyTournament', self.tournament);
                             requestStatus.startLoading('Copying');
                             var opensAt = $moment(self.tournament.openDate + " " +
                                                   self.tournament.startTime, 'Y-MM-DD HH:mm A').
                                 utc().format("Y-MM-DDTHH:mm:ss.SSS") + "Z";
                             var request = {name: self.tournament.name,
                                            opensAt: opensAt,
                                            originTid: $routeParams.tournamentId};
                             $http.post('/api/tournament/copy',
                                        request,
                                        {headers: {session: auth.mySession()}}).
                                 then(
                                     function (ok) {
                                         requestStatus.complete();
                                         $location.path('/my/tournament/' + ok.data);
                                     },
                                     requestStatus.failed);
                         };
                         requestStatus.startLoading();
                         Tournament.aMine({tournamentId: $routeParams.tournamentId},
                                              function (tournament) {
                                                  requestStatus.complete();
                                                  self.tournament.name = tournament.name;
                                                  self.tournament.startTime = $moment(tournament.opensAt).format('hh:mm A');
                                              },
                                              requestStatus.failed);
                     }]});
