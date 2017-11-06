import angular from 'angular';
import template from './manage-one.template.html';

angular.module('participant').
    component('manageOneParticipant', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                     'pageCtx', 'Participant', 'binder', '$scope',
                     function ($http, mainMenu, $routeParams, auth, requestStatus,
                               pageCtx, Participant, binder, $scope) {
                         this.tournamentId = $routeParams.tournamentId;
                         this.participant = null;
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 var ctxMenu = {};
                                 ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                                 mainMenu.setTitle('Participant', ctxMenu);
                             },
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading('Loading participant');
                                 Participant.state(
                                     {uid: $routeParams.userId,
                                      tournamentId: $routeParams.tournamentId},
                                     function (state) {
                                         requestStatus.complete();
                                         self.participant = state;
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
        });
