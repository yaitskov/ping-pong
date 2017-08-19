import angular from 'angular';

angular.
    module('core.requestStatus').
    factory('requestStatus', ['$rootScope', function ($rootScope) {
        return new function () {
            this.startLoading = function (msg, meta) {
                $rootScope.$broadcast('event.request.started', msg, meta);
            };

            this.failed = function (response, meta) {
                $rootScope.$broadcast('event.request.failed', response, meta);
            };

            this.validationFailed = function (message) {
                $rootScope.$broadcast('event.request.validation', message);
            };

            this.complete = function (response) {
                $rootScope.$broadcast('event.request.complete', response);
            };
        };
    }]);
