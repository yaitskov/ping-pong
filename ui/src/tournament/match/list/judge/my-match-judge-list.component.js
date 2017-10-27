import angular from 'angular';
import template from './my-match-judge-list.template.html';

angular.module('tournament').
    component('myMatchJudgeList', {
        templateUrl: template,
        controller: ['Match', 'Tournament', 'Participant', 'mainMenu', '$location',
                     'pageCtx', 'requestStatus', '$routeParams',
                     function (Match, Tournament, Participant, mainMenu, $location,
                               pageCtx, requestStatus, $routeParams) {
                         mainMenu.setTitle('Match Judgement');
                         var self = this;
                         self.matches = null;
                         self.tournamentId = $routeParams.tournamentId;
                         self.tournamentState = null;
                         self.bid = null;
                         self.bids = null;
                         self.completeMatch = function (match) {
                             pageCtx.put('last-scoring-match', match);
                             $location.path('/complete/match/' + match.mid);
                         };
                         self.bidChange = () => {
                             self.matches = null;
                             Match.bidMatchesNeedToPlay(
                                 {tournamentId: $routeParams.tournamentId,
                                  bid: self.bid
                                 },
                                 function (matches) {
                                     requestStatus.complete();
                                     self.matches = matches;
                                 },
                                 requestStatus.failed);

                         requestStatus.startLoading();
                         Tournament.parameters(
                             {tournamentId: $routeParams.tournamentId},
                             (rules) => {
                                 if (rules.place && rules.place.arenaDistribution == 'GLOBAL') {
                                     self.showTables = true;
                                     self.orderField = 'table.label';
                                     Match.myMatchesNeedToJudge(
                                         {tournamentId: $routeParams.tournamentId},
                                         (matches) => {
                                             requestStatus.complete();
                                             self.matches = matches;
                                             self.tournamentNotOpen = !matches.length;
                                         },
                                         requestStatus.failed);
                                 } else {
                                     self.orderField = 'enemy.name';
                                     self.showTables = false;
                                     Participant.findByState(
                                         {tid: $routeParams.tournamentId,
                                          states: ['Wait', 'Play']
                                         },
                                         (bids) => {
                                             requestStatus.complete();
                                             self.bids = bids;
                                             for (var i in bids) {
                                                 self.bid = bids[i].uid;
                                                 break;
                                             }
                                         },
                                         requestStatus.failed);
                                 }
                             },
                             requestStatus.failed);
                     }
                    ]
    });
