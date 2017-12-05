import angular from 'angular';

angular.
    module('core.group').
    factory('Group', ['$resource',
                      function ($resource) {
                          return $resource('/', {}, {
                              populations: {
                                  url: '/api/group/populations/:tournamentId/cid/:categoryId',
                                  method: 'GET'
                              },
                              infoWithMembers: {
                                  url: '/api/group/members/:tournamentId/:groupId',
                                  method: 'GET',
                                  headers: {
                                      session: 1
                                  }
                              }
                          });
                      }
                     ]);
