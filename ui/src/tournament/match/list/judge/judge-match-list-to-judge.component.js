import angular from 'angular';
import template from './judge-match-list-to-judge.template.html';

angular.module('tournament').
    component('judgeMatchListToJudge', {
        templateUrl: template,
        controller: ['Match', 'Tournament', 'Participant', 'mainMenu', '$location',
                     'pageCtx', 'requestStatus', '$routeParams', 'cutil', 'binder', '$scope',
                     function (Match, Tournament, Participant, mainMenu, $location,
                               pageCtx, requestStatus, $routeParams, cutil, binder, $scope) {
                         mainMenu.setTitle('Match Judgement');
                         var self = this;
                         self.matches = null;
                         self.tournamentId = $routeParams.tournamentId;
                         self.tournamentState = null;
                         self.bid = null;
                         self.bids = null;
                         self.completeMatch = function (match) {
                             if (self.bid) {
                                 match.participants = [
                                     match.enemy,
                                     {uid: self.bid,
                                      name: cutil.findValBy(self.bids, {uid: +self.bid}).name}];
                             }
                             pageCtx.put('last-scoring-match', match);
                             $location.path('/complete/match/' + match.mid);
                         };
                         self.bidChange = () => {
                             self.matches = null;
                             pageCtx.put('last-bid', self.bid);
                             Match.bidMatchesNeedToPlay(
                                 {tournamentId: $routeParams.tournamentId,
                                  bid: self.bid
                                 },
                                 function (matches) {
                                     requestStatus.complete();
                                     self.matches = matches.matches;
                                     self.progress = matches.progress;
                                 },
                                 requestStatus.failed);
                         };

                         binder($scope, {
                             'event.request.status.ready': (event) => {
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
                                                     self.matches = matches.matches;
                                                     self.progress = matches.progress;
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
                                                     if (bids.length) {
                                                         self.bid = "" + cutil.findValByO(
                                                             bids, {uid: +pageCtx.get('last-bid')}, bids[0]).uid;
                                                         self.bidChange();
                                                     }
                                                 },
                                                 requestStatus.failed);
                                         }
                                     },
                                     requestStatus.failed);
                             }
                         });

                     }
                    ]
    });
