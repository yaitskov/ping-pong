'use strict';

angular.module('devPingPong').
    config(['$locationProvider', '$routeProvider',
            function config($locationProvider, $routeProvider) {
                $locationProvider.hashPrefix('!');
                $routeProvider.
                    when('/emails', {
                        template: '<emails-with-sign-in-token-list></emails-with-sign-in-token-list>'
                    }).
                    otherwise('/emails');
            }
           ]);
