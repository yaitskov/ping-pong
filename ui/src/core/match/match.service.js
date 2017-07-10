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
                                   }
                               });
                           }
                          ]);
