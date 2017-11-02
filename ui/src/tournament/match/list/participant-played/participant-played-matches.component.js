import angular from 'angular';
import template from './participant-played-matches.template.html';

angular.
    module('tournament').
    component('participantPlayedMatches', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', '$routeParams', 'requestStatus',
                     function (Match, mainMenu, $routeParams, requestStatus) {
                         mainMenu.setTitle('Matches played by me');
                         var self = this;
                         requestStatus.startLoading();
                         Match.myPlayedMatches(
                             {tournamentId: $routeParams.tournamentId},
                             function (matches) {
                                 requestStatus.complete();
                                 self.matches = matches;
                             },
                             requestStatus.failed);
                     }
                    ]
        });
