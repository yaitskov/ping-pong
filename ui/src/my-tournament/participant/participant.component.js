import angular from 'angular';
import template from './participant.template.html';

angular.module('participant').
    component('participant', {
        templateUrl: template,
        controller: ['$http', 'mainMenu', '$routeParams', 'auth', 'requestStatus',
                     'pageCtx', 'Participant', '$translate',
                     function ($http, mainMenu, $routeParams, auth, requestStatus,
                               pageCtx, Participant, $translate) {
                         $translate(['Participant', 'Tournament']).then(function (translations) {
                             mainMenu.setTitle(translations.Participant);
                             var ctxMenu = {};
                             ctxMenu['#!/my/tournament/' + $routeParams.tournamentId] = translations.Tournament;
                         });
                         mainMenu.setContextMenu(ctxMenu);
                         this.tournamentId = $routeParams.tournamentId;
                         this.participant = null;
                         var self = this;
                         $translate('Loading participant').then(function (msg) {
                             requestStatus.startLoading(msg);
                             Participant.state(
                                 {uid: $routeParams.userId,
                                  tournamentId: $routeParams.tournamentId},
                                 function (state) {
                                     requestStatus.complete();
                                     self.participant = state;
                                 },
                                 requestStatus.failed);
                         });
                     }
                    ]
        });
