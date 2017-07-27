'use strict';

angular.module('core', ['core.tournament', 'core.match',
                        'localStorage', 'core.place',
                        'core.validate', 'core.category',
                        'core.requestStatus', 'core.ui']).
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
