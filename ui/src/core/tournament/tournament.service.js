import angular from 'angular';

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
                                       url: '/api/tournament/running/:alsoCompleteInDays',
                                       method: 'GET',
                                       cache: false,
                                       isArray: true
                                   },
                                   administered: {
                                       url: '/api/tournament/editable/by/me/:completeInDays',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       },
                                       isArray: true
                                   },
                                   result: {
                                       url: '/api/tournament/result/:tournamentId/category/:categoryId',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       },
                                       isArray: true
                                   },
                                   participateIn: {
                                       url: '/api/tournament/enlisted/:completeAfterDays',
                                       method: 'GET',
                                       cache: false,
                                       headers: {
                                           session: 1
                                       },
                                       isArray: true
                                   },
                                   // info: {
                                   //     url: '/api/tournament/:tournamentId',
                                   //     method: 'GET',
                                   //     cache: false
                                   // },
                                   enlistOffline: {
                                       url: '/api/tournament/enlist-offline',
                                       method: 'POST',
                                       isArray: false,
                                       responseType: 'text',
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   state: {
                                       url: '/api/tournament/state',
                                       method: 'POST',
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   expel: {
                                       url: '/api/tournament/expel',
                                       method: 'POST',
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   resign: {
                                       url: '/api/tournament/resign',
                                       method: 'POST',
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   parameters: {
                                       url: '/api/tournament/params/:tournamentId',
                                       method: 'GET',
                                       cache: false
                                   },
                                   updateParams: {
                                       url: '/api/tournament/params/:tournamentId',
                                       method: 'POST',
                                       headers: {
                                           session: 1
                                       }
                                   },
                                   aComplete: {
                                       url: '/api/tournament/complete/:tournamentId',
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
