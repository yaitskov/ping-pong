import angular from 'angular';
import template from './participant-result.template.html';

angular.
    module('participant').
    component('participantResult', {
        templateUrl: template,
        controller: ['Participant', 'mainMenu', '$routeParams', 'requestStatus', 'binder', '$scope',
                     function (Participant, mainMenu, $routeParams, requestStatus, binder, $scope) {
                         mainMenu.setTitle('Participant results');
                         var self = this;
                         binder($scope, {
                             'event.request.status.ready': (event) => {
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
                         });
                     }
                    ]});
