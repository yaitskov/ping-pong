import angular from 'angular';

angular.
    module('core.category').
    factory('Category', ['$resource',
                         function ($resource) {
                             return $resource('/api/category', {}, {
                                 members: {
                                     url: '/api/category/find/members/:tournamentId/:categoryId',
                                     method: 'GET',
                                     headers: {
                                         session: 1
                                     }
                                 },
                                 ofTournament: {
                                     url: '/api/category/find/by/tid/:tid',
                                     method: 'GET',
                                     cache: false,
                                     headers: {
                                         session: 1
                                     },
                                     isArray: true
                                 },
                                 deleteCat: {
                                     url: '/api/category/delete',
                                     headers: {session: true},
                                     method: 'POST'
                                 }
                             });
                         }
                        ]);
