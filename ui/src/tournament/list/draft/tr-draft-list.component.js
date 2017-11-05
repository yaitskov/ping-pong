import angular from 'angular';
import template from './tr-draft-list.template.html';

angular.module('tournament').
    component('tournamentDraftList', {
        templateUrl: template,
        controller: ['Tournament', 'auth', 'mainMenu', '$location', 'requestStatus', 'binder', '$scope',
                     function (Tournament, auth, mainMenu, $location, requestStatus, binder, $scope) {
                         mainMenu.setTitle('Drafting');
                         var self = this;
                         self.tournaments = null;
                         self.goTo = function (tid) {
                             $location.path('/tournaments/' + tid);
                         };
                         binder($scope, {
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Tournament.drafting(
                                     {},
                                     function (tournaments) {
                                         self.tournaments = tournaments;
                                         requestStatus.complete(tournaments);
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
        });
