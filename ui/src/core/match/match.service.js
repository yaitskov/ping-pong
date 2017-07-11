'use strict';

angular.
    module('core.match').
    factory('Match', ['$resource', 'auth', '$routeParams',
                           function ($resource, auth, $routeParams) {
                               return $resource('/', {}, {
                                   listOpenForWatch: {
                                       url: '/api/match/watch/list/open/:tournamentId',
                                       method: 'GET',
                                       isArray: true
                                   },
                                   myMatchesNeedToPlay: {
                                       url: '/api/match/list/my/pending',
                                       method: 'GET',
                                       isArray: true,
                                       headers: {
                                           session: auth.mySession()
                                       }
                                   },
                                   myMatchesNeedToJudge: {
                                       url: '/api/match/judge/list/open',
                                       method: 'GET',
                                       isArray: true,
                                       headers: {
                                           session: auth.mySession()
                                       }
                                   },
                                   winners: {
                                       url: '/api/match/tournament-winners/:tournamentId',
                                       method: 'GET',
                                       isArray: true
                                   },
                                   scoreMatch: {
                                       url: '/api/match/participant/score',
                                       method: 'POST',
                                       headers: {
                                           'Content-Type': 'application/json',
                                           session: auth.mySession()}
                                   }
                               });
                           }
                          ]);
