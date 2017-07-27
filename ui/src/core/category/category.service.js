'use strict';

angular.
    module('core.category').
    factory('Category', ['$resource',
                         function ($resource) {
                             return $resource('/api/category', {}, {
                                 members: {
                                     url: '/api/category/find/members/:categoryId',
                                     method: 'GET',
                                     cache: false,
                                     headers: {
                                         session: 1
                                     },
                                     isArray: true
                                 }
                             });
                         }
                        ]);
