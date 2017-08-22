import angular from 'angular';
import template from './tournament-result.template.html';

angular.
    module('tournamentResult').
    component('tournamentResult', {
        templateUrl: template,
        controller: ['Tournament', 'mainMenu', '$routeParams', 'requestStatus', '$translate',
                     function (Tournament, mainMenu, $routeParams, requestStatus, $translate) {
                         $translate('Tournament results').then(function (msg) {
                             mainMenu.setTitle(msg);
                         });
                         var self = this;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.currentCid = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};
                         this.pickCategory = function (cid) {
                             $translate("Loading participants").then(function (msg) {
                                 requestStatus.startLoading(msg);
                                 self.currentCid = cid;
                                 Tournament.result(
                                     {tournamentId: $routeParams.tournamentId,
                                      categoryId: cid},
                                     function (participants) {
                                         requestStatus.complete();
                                         self.currentCid = cid;
                                         self.participants = participants;
                                     },
                                     requestStatus.failed);
                             });
                         };
                         requestStatus.startLoading();
                         Tournament.aComplete(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 requestStatus.complete();
                                 self.tournament = tournament;
                                 tournament.tid = $routeParams.tournamentId;
                                 for (var i in tournament.categories) {
                                     var category = tournament.categories[i];
                                     self.pickCategory(category.cid);
                                     break;
                                 }
                             },
                             requestStatus.failed);
                     }
                    ]
        });
