'use strict';


angular.
    module('core').
    factory('cutil', [function () {
        return new function () {
            this.genUserSessionPart = function () {
                function s4() {
                    return Math.floor((1 + Math.random()) * 0x10000)
                        .toString(16)
                        .substring(1);
                }
                return s4() + s4() + s4() + s4() + s4();
            };
            this.findValBy = function (list, pattern) {
                next:
                for (var i in list) {
                    var item = list[i];
                    for (var k in Object.keys(pattern)) {
                        if (item[k] !== pattern[k]) {
                            continue next;
                        }
                    }
                    return item;
                }
                throw "no item with pattern " + pattern;
            };
        };
    }]).
    factory('pageCtx', [function () {
        return new function () {
            var map = {};
            this.put = function (key, value) {
                return map[key] = value;
            };
            this.get = function (key) {
                return map[key];
            };
        };
    }]);
