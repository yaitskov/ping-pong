'use strict';

angular.module('core', ['core.tournament', 'core.match',
                        'localStorage', 'core.place',
                        'core.requestStatus']).
    filter('isEmpty', function () {
        var bar;
        return function (obj) {
            for (bar in obj) {
                if (obj.hasOwnProperty(bar)) {
                    return false;
                }
            }
            return true;
        };
    });
