import 'angular';
import template from './judge-judged-matches.template.html';

angular.
    module('tournament').
    component('judgeJudgedMatches', {
        templateUrl: template,
        controller: ['Participant', 'Match', 'mainMenu', '$routeParams', 'requestStatus',
                     'binder', '$scope', 'cutil', 'pageCtx', 'eBarier', '$rootScope',
                     function (Participant, Match, mainMenu, $routeParams, requestStatus,
                               binder, $scope, cutil, pageCtx, eBarier, $rootScope) {
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;

                         var inGroupBarier = eBarier.create(['widget', 'data'], (list) => {
                             $rootScope.$broadcast('event.complete.match.list.data.inGroup', list);
                         });
                         var playOffBarier = eBarier.create(['widget', 'data'], (list) => {
                             $rootScope.$broadcast('event.complete.match.list.data.playOff', list);
                         });
                         self.bidChange = () => {
                             pageCtx.put('last-bid-complete', self.bid);
                             requestStatus.startLoading();
                             Match.judgedMatches(
                                 {tournamentId: $routeParams.tournamentId,
                                  participantId: self.bid},
                                 function (matches) {
                                     requestStatus.complete();
                                     self.matches = matches;
                                     inGroupBarier.got('data', matches.inGroup);
                                     playOffBarier.got('data', matches.playOff);
                                 },
                               (...a) => requestStatus.failed(...a));
                         };

                         binder($scope, {
                             'event.complete.match.list.ready.inGroup': (e) => inGroupBarier.got('widget'),
                             'event.complete.match.list.ready.playOff': (e) => playOffBarier.got('widget'),
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Judged matches'),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Participant.findWithMatch(
                                     {tournamentId: $routeParams.tournamentId},
                                     (bids) => {
                                         requestStatus.complete();
                                         self.bids = bids;
                                         if (bids.length) {
                                             self.bid = "" + cutil.findValByO(
                                                 bids, {uid: +pageCtx.get('last-bid-complete')}, bids[0]).uid;
                                             self.bidChange();
                                         }
                                     },
                                   (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]
        });
