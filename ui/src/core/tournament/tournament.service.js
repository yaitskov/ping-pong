'use strict';

angular.
    module('core.tournament').
    factory('Tournament', ['$resource',
                           function ($resource) {
                               return $resource('/api/tournament', {}, {
                                   drafting: {
                                       url: '/api/tournament/drafting',
                                       method: 'GET',
                                       cache: false,
                                       isArray: true
                                   },
                                   running: {
                                       url: '/api/tournament/running',
                                       method: 'GET',
                                       cache: false,
                                       isArray: true
                                   },
                                   administered: {
                                       url: '/api/tournament/editable/by/me',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       },
                                       isArray: true
                                   },
                                   participateIn: {
                                       url: '/api/tournament/enlisted',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       },
                                       isArray: true
                                   },
                                   info: {
                                       url: '/api/tournament/:tournamentId',
                                       method: 'GET',
                                       cache: false
                                   },
                                   aMine: {
                                       url: '/api/tournament/mine/:tournamentId',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   myRecent: {
                                       url: '/api/tournament/my-recent',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   myRecentJudgements: {
                                       url: '/api/tournament/my-recent-judgement',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   aDrafting: {
                                       url: '/api/tournament/drafting/:tournamentId',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   update: {
                                       url: '/api/tournament/update',
                                       method: 'POST',
                                       headers: {
                                           session: 1
                                       }
                                   }
                               });
                           }
                          ]);
