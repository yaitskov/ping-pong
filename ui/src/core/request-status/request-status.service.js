import angular from 'angular';

angular.
    module('core.requestStatus').
    factory('requestStatus', ['$rootScope', 'syncTranslate', function ($rootScope, syncTranslate) {
        return new function () {
            var stranslate = syncTranslate.create();
            var self = this;
            this.startLoading = function (originMsg, meta) {
                stranslate.trans(originMsg || 'Loading', function (msg) {
                    $rootScope.$broadcast('event.request.started', msg, meta);
                });
            };

            this.failed = function (response, meta) {
                $rootScope.$broadcast('event.request.failed', response, meta);
            };

            this.validationFailed = function (originMsg) {
                stranslate.trans(originMsg, function (msg) {
                    $rootScope.$broadcast('event.request.validation', msg);
                });
            };

            this.complete = function (response) {
                $rootScope.$broadcast('event.request.complete', response);
            };
        };
    }]);
