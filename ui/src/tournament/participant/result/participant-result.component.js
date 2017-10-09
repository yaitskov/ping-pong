import angular from 'angular';
import template from './participant-result.template.html';

angular.
    module('participant').
    component('participantResult', {
        templateUrl: template,
        controller: ['Participant', 'mainMenu', '$routeParams', 'requestStatus',
                     function (Participant, mainMenu, $routeParams, requestStatus) {
                         mainMenu.setTitle('Participant results');
                         var self = this;
                         requestStatus.startLoading();
                         Participant.getResults(
                             {tournamentId: $routeParams.tournamentId,
                              uid: $routeParams.participantId},
                             function (results) {
                                 requestStatus.complete();
                                 self.results = results;
                             },
                             requestStatus.failed);
                     }
                    ]});
