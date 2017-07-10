'use strict';

angular.
    module('core.tournament').
    factory('Tournament', ['$resource', 'auth',
                           function ($resource, auth) {
                               return $resource('/api/tournament', {}, {
                                   drafting: {
                                       url: '/api/tournament/drafting',
                                       method: 'GET',
                                       isArray: true
                                   },
                                   running: {
                                       url: '/api/tournament/running',
                                       method: 'GET',
                                       isArray: true
                                   },
                                   administered: {
                                       url: '/api/tournament/editable/by/me',
                                       method: 'GET',
                                       headers: {
                                           session: auth.mySession()
                                       },
                                       isArray: true
                                   },
                                   info: {
                                       url: '/api/tournament/:tournamentId',
                                       method: 'GET'
                                   },
                                   aMine: {
                                       url: '/api/tournament/mine/:tournamentId',
                                       method: 'GET',
                                       headers: {
                                           session: auth.mySession()
                                       }
                                   },
                                   aDrafting: {
                                       url: '/api/tournament/drafting/:tournamentId',
                                       method: 'GET',
                                       headers: {
                                           session: auth.mySession()
                                       }
                                   }
                               });
                           }
                          ]);
