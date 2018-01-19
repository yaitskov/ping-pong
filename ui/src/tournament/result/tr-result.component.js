import angular from 'angular';
import 'css/medal.scss';
import '../tournament-result.scss';
import './tr-result.scss';
import template from './tr-result.template.html';

angular.
    module('tournament').
    component('tournamentResult', {
        templateUrl: template,
        controller: ['Tournament', 'mainMenu', '$routeParams', 'requestStatus', 'binder', '$scope',
                     function (Tournament, mainMenu, $routeParams, requestStatus, binder, $scope) {
                         var self = this;
                         self.matches = null;
                         self.winners = null;
                         self.tournament = null;
                         self.currentCid = null;
                         self.tid = $routeParams.tournamentId;
                         var params = {tournamentId: $routeParams.tournamentId};
                         self.splitPlayOffAndGroupParticipants = function () {
                             var border = -1;
                             self.inGroupParticipants = null;
                             self.playOffParticipants = null;
                             for (var i = 1; i < self.participants.length; ++i) {
                                 if (self.participants[i - 1].playOffStep && !self.participants[i].playOffStep) {
                                    border = i;
                                    break;
                                 }
                             }
                             if (border < 0) {
                                 self.playOffParticipants = self.participants;
                             } else {
                                 self.playOffParticipants = self.participants.slice(0, border);
                                 self.inGroupParticipants = self.participants.slice(border);
                             }
                             self.numOfPlayOffParticipants = self.playOffParticipants.length;
                         };
                         self.pickCategory = function (cid) {
                             requestStatus.startLoading("Loading participants");
                             self.currentCid = cid;
                             Tournament.result(
                                 {tournamentId: $routeParams.tournamentId,
                                  categoryId: cid},
                                 function (participants) {
                                     requestStatus.complete();
                                     self.currentCid = cid;
                                     self.participants = participants;
                                     self.splitPlayOffAndGroupParticipants();
                                 },
                                 requestStatus.failed);
                         };
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Tournament results'),
                             'event.request.status.ready': (event) => {
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
                         });
                     }
                    ]
        });
