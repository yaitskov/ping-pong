import angular from 'angular';
import template from './participant-profile.template.html';

angular.
    module('participant').
    component('participantProfile', {
        templateUrl: template,
        controller: ['Participant', 'pageCtx', 'mainMenu', '$routeParams', 'requestStatus',
                     function (Participant, pageCtx, mainMenu, $routeParams, requestStatus) {
                         mainMenu.setTitle('Participant lbl');
                         var self = this;
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
                    ]});
