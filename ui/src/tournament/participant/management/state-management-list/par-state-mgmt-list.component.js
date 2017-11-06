import angular from 'angular';
import template from './par-state-mgmt-list.template.html';

angular.module('participant').
    component('parStateMgmtList', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                     'Participant', 'Tournament', 'binder', '$scope',
                     function ($http, mainMenu, $routeParams, auth, requestStatus,
                               Participant, Tournament, binder, $scope) {
                         this.tournamentId = $routeParams.tournamentId;
                         this.enlisted = null;
                         this.toBeExpelled = null;
                         var self = this;
                         this.expelUrl = function (tid, uid) {
                             return '#!/my/tournament/' + tid + '/participant/' + uid;
                         };
                         this.confirmExpel = function (enlisted) {
                             self.toBeExpelled = enlisted;
                             jQuery('#confirmParticipantExpel').modal('show');
                         }
                         this.expel = function (enlisted) {
                             requestStatus.startLoading('Expelling');
                             Tournament.expel(
                                 {uid: enlisted.user.uid, tid: self.tournamentId},
                                 function (ok) {
                                     requestStatus.complete();
                                     enlisted.state = 'Expl';
                                 },
                                 requestStatus.failed);
                         }
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
                                 requestStatus.failed);
                         };
                         this.participantIsHere= function (participant) {
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
                                 requestStatus.failed);
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
                                 requestStatus.failed);
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
                                         requestStatus.failed);
                             }
                         });
                     }
                    ]
        });
