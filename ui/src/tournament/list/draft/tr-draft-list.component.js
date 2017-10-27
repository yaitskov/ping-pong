import angular from 'angular';
import template from './tr-draft-list.template.html';

angular.module('tournament').
    component('tournamentDraftList', {
        templateUrl: template,
        controller: ['Tournament', 'auth', 'mainMenu', '$location', 'requestStatus',
                     function (Tournament, auth, mainMenu, $location, requestStatus) {
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
