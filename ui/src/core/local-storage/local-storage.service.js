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
            this.drop = function (key) {
                localStorage.removeItem(key);
            }
            this.allKeys = function () {
                var result = [];
                for (var i = 0; i < localStorage.length; ++i) {
                    result.push(localStorage.key(i));
                }
                return result;
            };
            this.asMap = function () {
                var keys = this.allKeys();
                var result = {};
                for (var i in keys) {
                    result[keys[i]] = this.get(keys[i]);
                }
                return result;
            };

        };
    }]);
