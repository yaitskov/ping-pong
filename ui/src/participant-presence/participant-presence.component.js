'use strict';

angular.module('participantPresence').
    component('participantPresence', {
        templateUrl: 'participant-presence/participant-presence.template.html',
        controller: ['$http', 'mainMenu', '$routeParams', 'auth',
                     function ($http, mainMenu, $routeParams, auth) {
                         mainMenu.setTitle('Checking presence of participants');
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         mainMenu.setContextMenu(ctxMenu);
                         this.enlisted = null;
                         var self = this;
                         this.flipParticipant = function (idx) {
                             console.log("check participant " + idx);
                             var participant = self.enlisted[idx];
                             if (participant['state'] == 'Here') {
                                 $http.post('/api/bid/disappeared',
                                            {tid: $routeParams.tournamentId,
                                             uid: self.enlisted[idx]['user']['uid']},
                                            {headers: {session: auth.mySession()}}).
                                     then(
                                         function (okResp) {
                                             participant['state'] = 'Want';
                                             self.error = '';
                                         },
                                         function (failResp) {
                                             self.error = "failed cancel ";
                                         });
                             } else {
                                 $http.post('/api/bid/ready-to-play',
                                            {tid: $routeParams.tournamentId,
                                             uid: self.enlisted[idx].user.uid},
                                            {headers: {session: auth.mySession()}}).
                                     then(
                                         function (okResp) {
                                             participant.state = 'Here';
                                             self.error = '';
                                         },
                                         function (failResp) {
                                             self.error = "failed to approve participation";
                                         });
                             }
                         };
                         $http.get('/api/bid/enlisted-to-be-checked/' + $routeParams.tournamentId,
                                   {headers: {session: auth.mySession()}}).
                             then(
                                 function (okResp) {
                                     self.enlisted = okResp.data;
                                     console.log("enlisted");
                                 },
                                 function (badResp) {
                                     console.log("failed get enlisted");
                                 });

                     }
                    ]
        });
