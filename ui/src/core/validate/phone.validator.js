import angular from 'angular';

angular.module('core.validate.phone', []).
    directive('validatePhone', function() {
        var phoneRegexp = /^[+]?([0-9]+[ -]?)+$$/;
        return {
            require: 'ngModel',
            restrict: 'A',
            link: function(scope, elm, attrs, ctrl) {
                if (ctrl) {
                    ctrl.$validators.phone = function(modelValue) {
                        return ctrl.$isEmpty(modelValue) || phoneRegexp.test(modelValue);
                    };
                }
            }
        };
    });
