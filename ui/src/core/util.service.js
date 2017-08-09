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
            this.findValByO = function (list, pattern, other) {
                next:
                for (var i in list) {
                    var item = list[i];
                    var keys = Object.keys(pattern);
                    for (var k in keys) {
                        if (item[keys[k]] !== pattern[keys[k]]) {
                            continue next;
                        }
                    }
                    return item;
                }
                return other;
            };
            this.has = function (item, set) {
                for (var i in set) {
                    if (set[i] == item) {
                        return true;
                    }
                }
                return false;
            }
            this.findValBy = function (list, pattern) {
                next:
                for (var i in list) {
                    var item = list[i];
                    var keys = Object.keys(pattern);
                    for (var k in keys) {
                        if (item[keys[k]] !== pattern[keys[k]]) {
                            continue next;
                        }
                    }
                    return item;
                }
                throw "no item with pattern " + pattern;
            };
        };
    }]).
    factory('countDown', ['$interval', function ($interval) {
        return new function () {
            var self = this;
            this.seconds = function ($scope, duration, tickCb, endCb) {
                tickCb(duration);
                var ctx = {};
                ctx.timer = $interval(function (counter) {
                    if (counter > duration) {
                        $interval.cancel(ctx.timer);
                        endCb();
                    } else {
                        tickCb(duration - counter);
                    }
                }, 1000, 0);
                $scope.$on('$destroy', function () {
                    $interval.cancel(ctx.timer);
                });
            };
        };
    }]).
    factory('refresher', ['$interval', function ($interval) {
        return new function () {
            var self = this;
            this.seconds = function ($scope, period, tickCb) {
                tickCb();
                var timer = $interval(tickCb, period);
                $scope.$on('$destroy', function () {
                    $interval.cancel(timer);
                });
            };
        };
    }]).
    factory('pageCtx', [function () {
        return new function () {
            var map = sessionStorage;
            this.put = function (key, value) {
                map.setItem(key, JSON.stringify({v: value, d: new Date()}));
            };
            this.get = function (key) {
                var result = map.getItem(key);
                if (result) {
                    result = JSON.parse(result);
                    return result.v;
                }
                return null;
            };
            this._enemyUidKey = function (myUid, mid) {
                return 'enemy-of-' + myUid + '-in-mid-' + mid;
            };
            this.putEnemyUid = function (myUid, mid, enemyUid) {
                this.put(this._enemyUidKey(myUid, mid), enemyUid);
            };
            this.getEnemyUid = function (myUid, mid) {
                return this.get(this._enemyUidKey(myUid, mid));
            };
            this.putMatchParticipants = function (mid, participants) {
                this.put('match-participants-' + mid, participants);
            };
            this.getMatchParticipants = function (mid) {
                return this.get('match-participants-' + mid);
            };
        };
    }]);
