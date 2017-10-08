import angular from 'angular';

angular.
    module('core.group').
    factory('Group', ['$resource',
                      function ($resource) {
                          return $resource('/', {}, {
                              populations: {
                                  url: '/api/group/populations/:tournamentId/cid/:categoryId',
                                  method: 'GET'
                              }
                          });
                      }
                     ]);
