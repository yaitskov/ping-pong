'use strict';

angular.module('core.table').
    factory('Table', ['$resource', 'auth',
                      function ($resource, auth) {
                          return $resource('/api/table', {}, {
                              setState: {
                                  url: '/api/table/state',
                                  method: 'POST',
                                  headers: {
                                      session: 1
                                  }
                              },
                              add: {
                                  url: '/api/table/create',
                                  method: 'POST',
                                  headers: {
                                      session: 1
                                  }
                              }
                          });
                      }]);
