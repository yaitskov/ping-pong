'use strict';

angular.
    module('tournamentResult').
    component('tournamentResult', {
        templateUrl: 'tournament-result/tournament-result.template.html',
        controller: ['Tournament', 'mainMenu', '$routeParams', 'requestStatus',
                     function (Tournament, mainMenu, $routeParams, requestStatus) {
                         mainMenu.setTitle('Tournament results');
                         var self = this;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.currentCid = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};
                         this.pickCategory = function (cid) {
                             requestStatus.startLoading("Loading participants");
                             Tournament.result(
                                 {tournamentId: $routeParams.tournamentId,
                                  categoryId: cid},
                                 function (participants) {
                                     requestStatus.complete();
                                     self.participants = participants;
                                 },
                                 requestStatus.failed);
                         }
                         requestStatus.startLoading();
                         Tournament.aComplete(
                             {tournamentId: $routeParams.tournamentId},
                             function (tournament) {
                                 requestStatus.complete();
                                 self.tournament = tournament;
                                 tournament.tid = $routeParams.tournamentId;
                                 for (var i in tournament.categories) {
                                     var category = tournament.categories[i];
                                     self.currentCid = category.cid;
                                     self.pickCategory(category.cid);
                                     break;
                                 }
                             },
                             requestStatus.failed);
                     }
                    ]
        });
