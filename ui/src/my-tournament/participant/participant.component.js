'use strict';

angular.module('participant').
    component('participant', {
        templateUrl: 'my-tournament/participant/participant.template.html',
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus', 'pageCtx', 'Participant',
                     function ($http, mainMenu, $routeParams, auth, requestStatus, pageCtx, Participant) {
                         mainMenu.setTitle('Participant');
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         mainMenu.setContextMenu(ctxMenu);
                         this.tournamentId = $routeParams.tournamentId;
                         this.participant = null;
                         var self = this;
                         this.expel = function () {
                         };
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
                    ]
        });
