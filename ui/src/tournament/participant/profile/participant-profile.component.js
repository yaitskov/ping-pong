import angular from 'angular';
import template from './participant-profile.template.html';

angular.
    module('participant').
    component('participantProfile', {
        templateUrl: template,
        controller: ['Participant', 'pageCtx', 'mainMenu', '$routeParams',
                     'requestStatus', 'binder', '$scope',
                     function (Participant, pageCtx, mainMenu, $routeParams,
                               requestStatus, binder, $scope) {
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Participant lbl'),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Participant.profile(
                                     {tournamentId: $routeParams.tournamentId,
                                      participantId: $routeParams.participantId},
                                     function (profile) {
                                         requestStatus.complete();
                                         self.profile = profile;
                                     },
                                     (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]});
