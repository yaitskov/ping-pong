'use strict';

angular.module('core.validate').
    directive('validatePrice', function() {
        var priceRegexp = /^[1-9][0-1]*([.][0-9]{2})?$/;
        return {
            require: 'ngModel',
            restrict: 'A',
            link: function(scope, elm, attrs, ctrl) {
                if (ctrl) {
                    ctrl.$validators.price = function(modelValue) {
                        return ctrl.$isEmpty(modelValue) || priceRegexp.test(modelValue);
                    };
                }
            }
        };
    });
