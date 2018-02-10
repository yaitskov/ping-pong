import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './console-tr-params.template.html';

angular.
    module('tournament').
    component('consoleTrParams', {
        templateUrl: template,
        controller: ['auth', '$scope', '$rootScope', 'binder', 'requestStatus', '$http',
                     function (auth, $scope, $rootScope, binder, requestStatus, $http) {
                         var self = this;
                         self.playConsoleTournament = false;
                         self.createConsoleTournament = (tid) => {
                            requestStatus.startLoading();
                            $http.post('/api/tournament/console/create', tid,
                                       {headers: {'Content-Type': 'application/json',
                                                   session: auth.mySession()}}).
                                  then((ok) => {
                                      self.tournament.consoleTid = ok.data;
                                      if (self.tournament.rules.group) {
                                          self.tournament.rules.group.console = 'INDEPENDENT_RULES';
                                      }
                                      requestStatus.complete();
                                  }, requestStatus.failed);
                         };
                         $scope.$watch('$ctrl.playConsoleTournament', (newv, old) => {
                             if (!self.tournament) {
                                 return;
                             }
                             if (newv && !self.tournament.consoleTid) {
                                 self.createConsoleTournament(self.tournament.tid);
                             } else if (!newv && self.tournament.rules.group) {
                                 self.tournament.rules.group.console = 'NO';
                             }
                         });
                         self.onTournamentSet = (event, tournament) => {
                             self.tournament = tournament;
                             self.playConsoleTournament = tournament.rules.group &&
                                   tournament.rules.group.console != 'NO';
                         };
                         binder($scope, {
                             'event.tournament.rules.set': self.onTournamentSet
                         });
                         $rootScope.$broadcast('event.tournament.rules.console.ready');
                     }]});
