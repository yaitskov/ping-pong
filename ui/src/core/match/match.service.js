import angular from 'angular';

angular.
    module('core.match').
    factory('Match', ['$resource',
                      function ($resource) {
                          return $resource('/', {}, {
                              listOpenForWatch: {
                                  url: '/api/match/watch/list/open/:tournamentId',
                                  method: 'GET',
                                  isArray: true
                              },
                              myMatchesNeedToPlay: {
                                  url: '/api/match/list/my/pending/:tournamentId',
                                  method: 'GET',
                                  headers: {
                                      session: 1
                                  }
                              },
                              bidMatchesNeedToPlay: {
                                  method: 'GET',
                                  url: '/api/match/list/bid/pending/:tournamentId/:bid'
                              },
                              getRules: {
                                  url: '/api/match/rules/:tournamentId',
                                  method: 'GET'
                              },
                              resetSetScoreDownTo: {
                                  url: '/api/match/reset-set-score',
                                  method: 'POST',
                                  headers: {
                                      session: 1
                                  }
                              },
                              myMatchesNeedToJudge: {
                                  url: '/api/match/judge/list/open/:tournamentId',
                                  method: 'GET',
                                  headers: {
                                      session: 1
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
                                      session: 1
                                  }
                              }
                          });
                      }
                     ]);
