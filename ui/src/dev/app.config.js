'use strict';

angular.module('devPingPong').
    .config(['$httpProvider', function($httpProvider) {
        if (!$httpProvider.defaults.headers.get) {
            $httpProvider.defaults.headers.get = {};
        }
        $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
        $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';
    }]).config(
        ['$locationProvider', '$routeProvider',
         function config($locationProvider, $routeProvider) {
             $locationProvider.hashPrefix('!');
             $routeProvider.
                 when('/emails', {
                     template: '<emails-with-sign-in-token-list></emails-with-sign-in-token-list>'
                 }).
                 otherwise('/emails');
         }
        ]);
