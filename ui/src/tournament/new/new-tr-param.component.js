import angular from 'angular';
import template from './new-tr-param.template.html';
import defaultMatchRules from './default-match-rules.js';
import defaultTournamentRules from './default-tournament-rules.js';

angular.
    module('tournament').
    component('newTournamentParameters', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location',
                     'pageCtx', 'requestStatus', 'moment', 'groupSchedule',
                     '$scope', '$rootScope', 'binder',
                     function (auth, mainMenu, $http, $location,
                               pageCtx, requestStatus, $moment, groupSchedule,
                               $scope, $rootScope, binder) {
                         var self = this;
                         self.tournament = pageCtx.get('newTournament') || {};
                         self.tournament.rules = Object.assign({},
                             defaultTournamentRules,
                             {match: defaultMatchRules[self.tournament.sport]},
                             self.tournament.rules || {});
                         self.published = self.tournament.tid;

                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Tournament Parameters'),
                             'event.tournament.rules.update': (event, rules) => {
                                 requestStatus.startLoading('Publishing');
                                 var req = angular.copy(self.tournament);
                                 req.opensAt =  $moment(req.openDate + " " + req.startTime, 'Y-MM-DD HH:mm A').
                                     utc().format("Y-MM-DDTHH:mm:ss.SSS") + "Z";
                                 req.rules = rules;
                                 delete req.placeName;
                                 delete req.openDate;
                                 delete req.startTime;
                                 $http.post('/api/tournament/create',
                                            req,
                                            {headers: {'Content-Type': 'application/json',
                                                       session: auth.mySession()}
                                            }).
                                     then(
                                         (okResp) => {
                                             self.tournament.tid = okResp.data;
                                             self.published = self.tournament.tid;
                                             pageCtx.put('newTournament', self.tournament);
                                             pageCtx.put('tournamentInfoForCategories',
                                                         {tid: self.tournament.tid,
                                                          name: self.tournament.name,
                                                          state: 'Hidden'});
                                             requestStatus.complete();
                                         },
                                         (resp) => {
                                             if (resp.data && resp.data.field2Errors) {
                                                 $rootScope.$broadcast('event.tournament.rules.errors',
                                                                       resp.data.field2Errors);

                                             }
                                             requestStatus.failed(resp);
                                         });
                             },
                             'event.tournament.rules.cancel': (event, rules) => {
                                 pageCtx.put('newTournament', null);
                                 $location.path('/tournament/draft/list');
                             },
                             'event.tournament.rules.back': (event, rules) => {
                                 pageCtx.put('newTournament', self.tournament);
                                 window.history.back();
                             },
                             'event.tournament.rules.ready': (event) => $rootScope.$broadcast('event.tournament.rules.set',
                                                               self.tournament.rules),
                         });
                     }
                    ]});
