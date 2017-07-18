'use strict';

angular.module('core.validate').
    directive('validateEmail', function() {
        var emailRegexp = /^[_a-z0-9]+(\.[_a-z0-9]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$/;

        return {
            require: 'ngModel',
            restrict: '',
            link: function(scope, elm, attrs, ctrl) {
                if (ctrl && ctrl.$validators.email) {
                    ctrl.$validators.email = function(modelValue) {
                        return ctrl.$isEmpty(modelValue) || emailRegexp.test(modelValue);
                    };
                }
            }
        };
    });