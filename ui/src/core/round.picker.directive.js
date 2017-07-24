'use strict';

angular.module('core.ui', ['ngResource']).
    directive('round-picker', ['$compile', function ($compile) {
        return {
            restrict: 'E',
            link: function (scope, elem, attrs) {
                var model = attrs['model'];
                var html = '<input type="hidden" ng-model="' + model + '" />';
                var el = $compile(html)(scope);
                elem.append(el);
                var input = elem.find('input[type=hidden]');
                elem.clockpicker({input: input});
            }
        };
    }]);
