import angular from 'angular';
import template from './tournament-list.template.html';

angular.module('tournamentList').
    component('tournamentList', {
        templateUrl: template,
        controller: ['Tournament', 'mainMenu', '$location', 'requestStatus',
                     function (Tournament, mainMenu, $location, requestStatus) {
                         mainMenu.setTitle('Drafting');
                         this.tournaments = null;
                         var self = this;
                         requestStatus.startLoading();
                         this.goTo = function (tid) {
                             $location.path('/tournaments/' + tid);
                         };
                         Tournament.drafting(
                             {},
                             function (tournaments) {
                                 self.tournaments = tournaments;
                                 requestStatus.complete(tournaments);
                             },
                             requestStatus.failed);
                     }
                    ]
        });
