'use strict';

angular.module('participantPresence').
    component('participantPresence', {
        templateUrl: 'participant-presence/participant-presence.template.html',
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus', 'Participant',
                     function ($http, mainMenu, $routeParams, auth, requestStatus, Participant) {
                         mainMenu.setTitle('Management of participants');
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         this.tournamentId = $routeParams.tournamentId;
                         mainMenu.setContextMenu(ctxMenu);
                         this.enlisted = null;
                         var self = this;
                         this.canExpel = function (idx) {
                             var state = this.enlisted[idx].state;
                             return state != 'Expl' && state != 'Quit';
                         };
                         this.canReset = function (idx) {
                             return self.enlisted[idx].state != 'Want';
                         };
                         this.canCheck = function (idx) {
                             return self.enlisted[idx].state == 'Paid';
                         };
                         this.canPay = function (idx) {
                             return self.enlisted[idx].state == 'Want';
                         };
                         this.markAsPaid = function (idx) {
                             requestStatus.startLoading("Paying");
                             Participant.setState(
                                 {uid: self.enlisted[idx].user.uid,
                                  tid: $routeParams.tournamentId,
                                  expected: 'Want',
                                  target: 'Paid'},
                                 function (ok) {
                                     requestStatus.complete();
                                     self.enlisted[idx].state = 'Paid';
                                 },
                                 requestStatus.failed);
                         };
                         this.participantIsHere= function (idx) {
                             requestStatus.startLoading("Marking participant presence");
                             Participant.setState(
                                 {uid: self.enlisted[idx].user.uid,
                                  tid: $routeParams.tournamentId,
                                  expected: 'Paid',
                                  target: 'Here'},
                                 function (ok) {
                                     requestStatus.complete();
                                     self.enlisted[idx].state = 'Here';
                                 },
                                 requestStatus.failed);
                         };
                         this.resetToWant = function (idx) {
                             requestStatus.startLoading("Reseting participant");
                             Participant.setState(
                                 {uid: self.enlisted[idx].user.uid,
                                  tid: $routeParams.tournamentId,
                                  expected: self.enlisted[idx].state,
                                  target: 'Want'},
                                 function (ok) {
                                     requestStatus.complete();
                                     self.enlisted[idx].state = 'Want';
                                 },
                                 requestStatus.failed);
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
