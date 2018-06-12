import angular from 'angular';
import './participant-presence.scss';
import template from './par-state-mgmt-list.template.html';

angular.module('participant').
    component('parStateMgmtList', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                     'Participant', 'Tournament', 'binder', '$scope', '$rootScope',
                     function ($http, mainMenu, $routeParams, auth, requestStatus,
                               Participant, Tournament, binder, $scope, $rootScope) {
                         this.tournamentId = $routeParams.tournamentId;
                         this.enlisted = null;
                         this.toBeExpelled = null;
                         var self = this;
                         this.confirmExpel = function (enlisted) {
                             enlisted.tid = self.tournamentId;
                             $rootScope.$broadcast('event.confirm-participant-expel.confirm', enlisted);
                         };
                         this.canExpel = function (participant) {
                             var state = participant.state;
                             return state != 'Expl' && state != 'Quit';
                         };
                         this.canReset = function (participant) {
                             return participant.state != 'Want';
                         };
                         this.canCheck = function (participant) {
                             return participant.state == 'Paid';
                         };
                         this.canPay = function (participant) {
                             return participant.state == 'Want';
                         };
                         this.markAsPaid = function (participant) {
                             requestStatus.startLoading("Paying");
                             Participant.setState(
                                 {uid: participant.user.uid,
                                  tid: $routeParams.tournamentId,
                                  expected: 'Want',
                                  target: 'Paid'},
                                 function (ok) {
                                     requestStatus.complete();
                                     participant.state = 'Paid';
                                 },
                               (...a) => requestStatus.failed(...a));
                         };
                         this.participantIsHere = function (participant) {
                             requestStatus.startLoading("Marking participant presence");
                             Participant.setState(
                                 {uid: participant.user.uid,
                                  tid: $routeParams.tournamentId,
                                  expected: 'Paid',
                                  target: 'Here'},
                                 function (ok) {
                                     requestStatus.complete();
                                     participant.state = 'Here';
                                 },
                               (...a) => requestStatus.failed(...a));
                         };
                         this.resetToWant = function (participant) {
                             requestStatus.startLoading("Reseting participant");
                             Participant.setState(
                                 {uid: participant.user.uid,
                                  tid: $routeParams.tournamentId,
                                  expected: participant.state,
                                  target: 'Want'},
                                 function (ok) {
                                     requestStatus.complete();
                                     participant.state = 'Want';
                                 },
                               (...a) => requestStatus.failed(...a));
                         };
                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 var ctxMenu = {};
                                 ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                                 mainMenu.setTitle('ManagementOfParticipants', ctxMenu);
                             },
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 $http.get('/api/bid/enlisted-to-be-checked/' + $routeParams.tournamentId,
                                           {headers: {session: auth.mySession()}}).
                                     then(
                                         function (okResp) {
                                             self.enlisted = okResp.data;
                                             requestStatus.complete();
                                         },
                                         (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]
        });
