'use strict';

angular.
    module('core.requestStatus').
    factory('requestStatus', ['$rootScope', function ($rootScope) {
        return new function () {
            this._entity = "Entity";
            this._badStatusExplantion = "is not in the proper state for the operation";
            this.setup = function (entity, badStatusMsg) {
                this._entity = entity;
                if (badStatusMsg) {
                    this._badStatusExplantion = badStatusMsg;
                }
            };
            this.startLoading = function (msg, meta) {
                $rootScope.$broadcast('event.request.started', msg, meta);
            };

            this.failed = function (response) {
                $rootScope.$broadcast('event.request.failed', response);
            };

            this.validationFailed = function (message) {
                $rootScope.$broadcast('event.request.validation', message);
            };

            this.complete = function (response) {
                $rootScope.$broadcast('event.request.complete', response);
            };
        };
    }]);
