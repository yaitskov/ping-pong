import angular from 'angular';
import template from './manage-tr-list.template.html';

angular.module('tournament').
    component('manageTournamentList', {
        templateUrl: template,
        controller: ['Tournament', 'mainMenu',
                     function (Tournament, mainMenu) {
                         mainMenu.setTitle('AdministratedTournaments', {'#!/tournament/new': 'AddTournament'});
                         this.tournaments = null;
                         var self = this;
                         Tournament.administered(
                             {completeInDays: 30},
                             function (tournaments) {
                                 self.tournaments = tournaments;
                             });
                     }
                    ]
        });
