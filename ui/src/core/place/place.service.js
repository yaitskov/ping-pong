import angular from 'angular';

angular.module('core.place').
    factory('Place', ['$resource', 'auth',
                      function ($resource, auth) {
                          return $resource('/api/places', {}, {
                              tables: {
                                  url: '/api/tables/by-place/:placeId',
                                  method: 'GET',
                                  headers: {
                                      session: 1
                                  },
                                  isArray: true
                              },
                              myPlaces: {
                                  url: '/api/places',
                                  method: 'GET',
                                  headers: {
                                      session: 1
                                  },
                                  isArray: true
                              },
                              change: {
                                  url: '/api/place/update',
                                  method: 'POST',
                                  headers: {
                                      session: 1
                                  }
                              },
                              aPlace: {
                                  url: '/api/place/:placeId',
                                  method: 'GET'
                              }
                          });
                      }]);
