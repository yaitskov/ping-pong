import angular from 'angular';
import template from './new-tr-param.template.html';

const defaultRules = {
    casting: {
        policy: 'ProvidedRating',
        direction: 'Decrease',
        splitPolicy: 'BalancedMix',
        providedRankOptions: {
            label: 'rating',
            minValue: 0,
            maxValue: 1000000
        }
    },
    match: {
        minGamesToWin: 11,
        minAdvanceInGames: 2,
        minPossibleGames: 0,
        setsToWin: 3
    },
    group: {
        quits: 2,
        groupSize: 8,
        schedule: {
            size2Schedule: {
                2: [0, 1],
                3: [0, 2, 0, 1, 1, 2]
            }
        }
    },
    playOff: {
        losings: 1,
        thirdPlaceMatch: 1
    }
};

angular.
    module('tournament').
    component('newTournamentParameters', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', '$http', '$location',
                     'pageCtx', 'requestStatus', 'moment', 'groupSchedule',
                     '$scope', '$rootScope', 'binder', 'lateEvent',
                     function (auth, mainMenu, $http, $location,
                      pageCtx, requestStatus, $moment, groupSchedule,
                      $scope, $rootScope, binder, lateEvent) {
                         mainMenu.setTitle('Tournament Parameters');
                         var self = this;
                         self.tournament = pageCtx.get('newTournament') || {};
                         self.tournament.rules = Object.assign({}, defaultRules, self.tournament.rules || {});
                         self.published = self.tournament.tid;

                         binder($scope, {
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
                                 $location.path('/tournaments');
                             },
                             'event.tournament.rules.back': (event, rules) => {
                                 pageCtx.put('newTournament', self.tournament);
                                 window.history.back();
                             }
                         });

                         lateEvent(() => $rootScope.$broadcast('event.tournament.rules.set',
                                                               self.tournament.rules));
                     }
                    ]});
