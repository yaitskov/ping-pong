import angular from 'angular';
import template from './participant-presence.template.html';

angular.module('participantPresence').
    component('participantPresence', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                     'Participant', 'Tournament', '$translate',
                     function ($http, mainMenu, $routeParams, auth, requestStatus,
                               Participant, Tournament, $translate) {
                         this.tournamentId = $routeParams.tournamentId;
                         $translate(['ManagementOfParticipants', 'Tournament']).then(function (translations) {
                             mainMenu.setTitle(translations.ManagementOfParticipants);
                             var ctxMenu = {};
                             ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = translations.Tournament;
                             mainMenu.setContextMenu(ctxMenu);
                         });
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
                             $translate('Expelling').then(function (expelMsg) {
                                 requestStatus.startLoading(expelMsg);
                                 Tournament.expel(
                                     {uid: enlisted.user.uid, tid: self.tournamentId},
                                     function (ok) {
                                         requestStatus.complete();
                                         enlisted.state = 'Expl';
                                     },
                                     requestStatus.failed);
                             });
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
                             $translate("Paying").then(function (msg) {
                                 requestStatus.startLoading(msg);
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
                             });
                         };
                         this.participantIsHere= function (participant) {
                             $translate("Marking participant presence").then(function (msg) {
                                 requestStatus.startLoading(msg);
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
                             });
                         };
                         this.resetToWant = function (participant) {
                             $translate("Reseting participant").then(function (msg) {
                                 requestStatus.startLoading(msg);
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
                             });
                         };
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
                    ]
        });
