import angular from 'angular';

angular.
    module('core.util', []).
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
    filter('shortDate', ['$filter', function ($filter) {
        return function (dt) {
            if (!dt) {
                return "";
            }
            return $filter('date')(dt, 'MMM d');
        };
    }]).
    filter('weekSmartMinute', ['$filter', function ($filter) {
        return function (dt) {
            if (!dt) {
                return "";
            }
            return $filter('date')(dt, 'EEE h:mm a').replace(/:00 /, ' ');
        };
    }]).
    filter('longDateTime', ['$filter', function ($filter) {
        return function (dt) {
            if (!dt) {
                return "";
            }
            return $filter('date')(dt, 'MMM d EEE h:mm a Z').replace(/:00 /, ' ');
        };
    }]).
    factory('longDateTime', ['$filter', function ($filter) {
        return function (dt) {
            if (!dt) {
                return "";
            }
            return $filter('date')(dt, 'MMM d EEE h:mm a Z').replace(/:00 /, ' ');
        };
    }]).
    filter('compactName',  function () {
        return function (name) {
            return name.split(' ').map(function (part) { return part.substr(0, 4); }).join(' ');
        };
    }).
    factory('syncTranslate', ['$translate', '$q', function ($translate, $q) {
        return new function () {
            this.create = function () {
                return new function () {
                    var self = this;
                    this.lastCallId = new Object();
                    this.callTranslate = function (originMessage) {
                        if (typeof originMessage == "string") {
                            return $translate(originMessage);
                        } else {
                            return $translate(originMessage[0], originMessage[1]);
                        }
                    };
                    this.transMenu = function (map, nextCallback) {
                        var callId = new Object();
                        self.lastCallId = callId;
                        var keys = Object.keys(map);
                        var origins = [];
                        for (var i = 0; i < keys.length; ++i) {
                            origins.push(map[keys[i]]);
                        }
                        $translate(origins).then(function (translations) {
                            if (self.lastCallId == callId) {
                                for (var i = 0; i < keys.length; ++i) {
                                    map[keys[i]] = translations[map[keys[i]]];
                                }
                                nextCallback(map);
                            } else {
                                console.log("Reject obsolete translations");
                            }
                        });
                    };
                    this.trans = function (originMessage, nextCallback) {
                        var callId = new Object();
                        self.lastCallId = callId;
                        self.callTranslate(originMessage).then(function (msg) {
                            if (self.lastCallId == callId) {
                                nextCallback(msg);
                            } else {
                                console.log("Reject obsolete translation: " + msg);
                            }
                        });
                    };
                    this.transTitleAndMenu = function (originTitle, originMenu, callback) {
                        var callId = new Object();
                        self.lastCallId = callId;
                        var keys = Object.keys(originMenu);
                        var origins = [];
                        for (var i = 0; i < keys.length; ++i) {
                            origins.push(originMenu[keys[i]]);
                        }
                        $q.all([self.callTranslate(originTitle).$promise, $translate(origins).$promise]).then(
                            function (responses) {
                                translations = responses[1];
                                if (self.lastCallId == callId) {
                                    for (var i = 0; i < keys.length; ++i) {
                                        originMenu[keys[i]] = translations[originMenu[keys[i]]];
                                    }
                                    nextCallback(responses[0], originMenu);
                                } else {
                                    console.log("Reject obsolete translations");
                                }
                            });
                    };
                };
            };
        };
    }]).
    factory('lateEvent', ['$timeout', function ($timeout) {
        return function (callback) {
            $timeout(callback, 0);
        };
    }]).
    factory('binder', ['$rootScope', function ($rootScope) {
        return ($scope, listenerMap) => {
            let cleaners = [];
            for (var topic in listenerMap) {
                    cleaners.push($rootScope.$on(topic, listenerMap[topic]));
            }
            $scope.$on('$destroy', () => cleaners.forEach(cleaner => cleaner()));
            return cleaners;
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
