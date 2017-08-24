import angular from 'angular';
import template from './participant.template.html';

angular.module('participant').
    component('participant', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                     'pageCtx', 'Participant',
                     function ($http, mainMenu, $routeParams, auth, requestStatus,
                               pageCtx, Participant) {
                         var ctxMenu = {};
                         ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = 'Tournament';
                         mainMenu.setTitle('Participant', ctxMenu);
                         this.tournamentId = $routeParams.tournamentId;
                         this.participant = null;
                         var self = this;
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
