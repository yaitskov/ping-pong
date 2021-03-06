import angular from 'angular';
import EventBarrierFactory from './angular/EventBarrierFactory.js';
import eventBinder from './angular/eventBinder.js';
import AjaxInfo from './angular/AjaxInfo.js';
import MessageBus from './MessageBus.js';
import ProtocolSwitcher from './ProtocolSwitcher.js';
import VoiceInput from 'ui/widget/person-name-field/VoiceInput.js';
import SyncTranslateFactory from './SyncTranslateFactory.js';

var humanizeDuration = require('humanize-duration');

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
            };
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
    filter('humanDuration', ['LocalStorage', function (LocalStorage) {
        return function (dt) {
            if (!dt) {
                return "";
            }
            return humanizeDuration(dt, {
                language: LocalStorage.get('myLang') || 'pl'
            });
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
    filter('percent', [function () {
        return function (left, full) {
            if (!full) {
                return '0%';
            }
            return ((full - left) / full * 100.0).toFixed(2) + "%";
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
    service('ProtocolSwitcher', ProtocolSwitcher).
    service('VoiceInput', VoiceInput).
    service('MessageBus', MessageBus).
    service('eBarier', EventBarrierFactory).
    service('AjaxInfo', AjaxInfo).
    factory('longDateTime', ['$filter', function ($filter) {
        return function (dt) {
            if (!dt) {
                return "";
            }
            return $filter('date')(dt, 'MMM d EEE h:mm a Z').replace(/:00 /, ' ');
        };
    }]).
    filter('initials', function () {
        return function (name) {
            if (name.indexOf(' ') < 0) {
                return name.substr(0, 2);
            }
            var nameParts = name.split(' ');
            return nameParts[0].substr(0, 1) + nameParts[1].substr(0, 1);
        };
    }).
    filter('compactName',  function () {
        return function (name) {
            if (name.indexOf(' ') < 0) {
                return name.substr(0, 9);
            }
            var nameParts = name.split(' ');
            if (nameParts.length == 1) {
               return name.substr(0, 4) + '..' + name.substr(name.length - 3, 3);
            }
            return nameParts[0].substr(0, 2) + ' ' + nameParts[1].substr(0, 3) +
                   '..' + nameParts[1].substr(nameParts[1].length - 1, 1);
        };
    }).
    service('syncTranslate', SyncTranslateFactory).
    factory('lateEvent', ['$timeout', function ($timeout) {
        return function (callback) {
            $timeout(callback, 0);
        };
    }]).
    factory('binder', () => eventBinder).
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
    factory('shuffleArray', function () {
        return (array) => {
            for (var i = array.length - 1; i > 0; i--) {
                var j = Math.floor(Math.random() * (i + 1));
                var temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
            return array;
        }
    }).
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
