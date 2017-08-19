import angular from 'angular';

angular.module('core.country').
    factory('Country', ['$resource', 'auth',
                      function ($resource, auth) {
                          return $resource('/api/country', {}, {
                              list: {
                                  url: '/api/country/list',
                                  method: 'GET',
                                  isArray: true
                              },
                              add: {
                                  url: '/api/country/create',
                                  method: 'POST',
                                  headers: {
                                      session: 1
                                  }
                              }
                          });
                      }]);
