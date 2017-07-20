'use strict';

angular.
    module('localStorage').
    factory('LocalStorage', [function () {
        return new function () {
            this.store = function(key, value) {
                localStorage.setItem(key, value)
            };
            this.get = function (key) {
                var value = localStorage.getItem(key)
                return value;
            };
            this.clearAll = function () {
                while (localStorage.length > 0) {
                    localStorage.removeItem(localStorage.key(0));
                }
            };
        };
    }]);
