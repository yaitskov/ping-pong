import angular from 'angular';

angular.
    module('core.suggestion').
    factory('Suggestion', ['$resource',
                         function ($resource) {
                             return $resource('/api/participant/suggestion', {}, {
                                 suggestions: {
                                     url: '/api/participant/suggestion',
                                     method: 'POST',
                                     headers: {
                                         session: 1
                                     },
                                     isArray: true
                                 }
                             });
                         }
                        ]);
