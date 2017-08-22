import angular from 'angular';
import template from './play-in-tournament-list.template.html';

angular.module('playInTournamentList').
    component('playInTournamentList', {
        templateUrl: template,
        cache: false,
        controller: ['Tournament', 'mainMenu', 'requestStatus', '$translate',
                     function (Tournament, mainMenu, requestStatus, $translate) {
                         $translate('Tournaments I am enlisted to').then(function (msg) {
                             mainMenu.setTitle(msg);
                         });
                         this.tournaments = null;
                         var self = this;
                         requestStatus.startLoading();
                         this.viewUrl = function (tournament) {
                             if (tournament.state == 'Close') {
                                 return '/tournament/result/' + tournament.tid;
                             }
                             return '/tournaments/' + tournament.tid;
                         }
                         Tournament.participateIn(
                             {completeAfterDays: 3},
                             function (tournaments) {
                                 requestStatus.complete();
                                 self.tournaments = tournaments;
                             },
                             requestStatus.failed);
                     }
                    ]
    });
