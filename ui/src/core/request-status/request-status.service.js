import angular from 'angular';

angular.
    module('core.requestStatus').
    factory('requestStatus', ['$rootScope', function ($rootScope) {
        return new function () {
            var self = this;
            this.startLoading = function (msg, meta) {
                $rootScope.$broadcast('event.request.started', msg || 'Loading', meta);
            };

            this.failed = function (response, meta) {
                $rootScope.$broadcast('event.request.failed', response, meta);
            };

            this.validationFailed = function (msg) {
                $rootScope.$broadcast('event.request.validation', msg);
            };

            this.complete = function (response) {
                $rootScope.$broadcast('event.request.complete', response);
            };
        };
    }]);
