import angular from 'angular';

angular.module('core.validate.email', []).
    directive('validateEmail', function() {
        var emailRegexp = /^[_A-Za-z0-9]+(\.[_A-Za-z0-9]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[A-Za-z]{2,4})$/;

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
