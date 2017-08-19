import angular from 'angular';

angular.module('core.city').
    factory('City', ['$resource', 'auth',
                      function ($resource, auth) {
                          return $resource('/api/city', {}, {
                              list: {
                                  url: '/api/city/list/:countryId',
                                  method: 'GET',
                                  isArray: true
                              },
                              add: {
                                  url: '/api/city/create',
                                  method: 'POST',
                                  headers: {
                                      session: 1
                                  }
                              }
                          });
                      }]);
