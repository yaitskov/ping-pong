'use strict';

angular.
    module('core.match').
    factory('Match', ['$resource', 'auth', '$routeParams',
                           function ($resource, auth, $routeParams) {
                               return $resource('/api/match/watch/list/open/:tournamentId', {}, {
                                   listOpenForWatch: {
                                       url: '/api/match/watch/list/open/:tournamentId',
                                       method: 'GET',
                                       isArray: true
                                   }
                               });
                           }
                          ]);
