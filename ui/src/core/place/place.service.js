'use strict';

angular.module('core.place').
    factory('Place', ['$resource', 'auth',
                      function ($resource, auth) {
                          return $resource('/api/places', {}, {
                              myPlaces: {
                                  url: '/api/places',
                                  method: 'GET',
                                  headers: {
                                      session: auth.mySession()
                                  },
                                  isArray: true
                              },
                              aPlace: {
                                  url: '/api/place/:placeId',
                                  method: 'GET'
                              }
                          });
                      }]);
