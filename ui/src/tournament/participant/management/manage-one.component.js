import angular from 'angular';
import template from './manage-one.template.html';

angular.module('participant').
    component('manageOneParticipant', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                     'pageCtx', 'Participant', 'binder', '$scope', 'Tournament',
                     function ($http, mainMenu, $routeParams, auth, requestStatus,
                               pageCtx, Participant, binder, $scope, Tournament) {
                         this.tournamentId = $routeParams.tournamentId;
                         this.participant = null;
                         const self = this;
                         self.setState = (state) => {
                             requestStatus.startLoading();
                             Tournament.expel(
                                 {uid: self.participant.user.uid,
                                  targetBidState: state,
                                  tid: self.tournamentId},
                                 function (ok) {
                                     requestStatus.complete();
                                     participant.state = state;
                                 },
                                 requestStatus.failed);
                         };
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
