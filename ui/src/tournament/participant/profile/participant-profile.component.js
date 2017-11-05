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
                         mainMenu.setTitle('Participant lbl');
                         var self = this;
                         binder($scope, {
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Participant.profile(
                                     {tournamentId: $routeParams.tournamentId,
                                      participantId: $routeParams.participantId},
                                     function (profile) {
                                         requestStatus.complete();
                                         self.profile = profile;
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]});
