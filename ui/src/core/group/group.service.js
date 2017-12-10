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
                              },
                              list: {
                                  url: '/api/group/list/:tournamentId',
                                  method: 'GET',
                                  isArray: false,
                              },
                              result: {
                                  url: '/api/group/result/:tournamentId/:groupId',
                                  method: 'GET',
                                  isArray: false,
                              }
                          });
                      }
                     ]);
