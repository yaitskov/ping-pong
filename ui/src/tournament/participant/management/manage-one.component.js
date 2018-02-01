import angular from 'angular';
import template from './manage-one.template.html';

const BidTerminalStates = new Set(['Quit', 'Expl', 'Win1', 'Win2', 'Win3']);

angular.module('participant').
    component('manageOneParticipant', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                     'Participant', 'binder', '$scope', '$rootScope',
                     function ($http, mainMenu, $routeParams, auth, requestStatus,
                               Participant, binder, $scope, $rootScope) {
                         this.tournamentId = $routeParams.tournamentId;
                         this.participant = null;
                         const self = this;
                         self.expel = () => {
                             self.participant.tid = self.tournamentId;
                             $rootScope.$broadcast('event.confirm-participant-expel.confirm', self.participant);
                         };
                         this.BidTerminalStates = BidTerminalStates;
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
