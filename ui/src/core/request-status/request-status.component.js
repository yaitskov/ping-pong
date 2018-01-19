import './request-status.scss';
import angular from 'angular';
import template from './request-status.template.html';

angular.
    module('core.requestStatus').
    component('requestStatus', {
        templateUrl: template,
        controller: [
            'requestStatus', '$scope', '$anchorScroll', '$timeout',
            '$route', '$translate', 'syncTranslate', '$sce', 'auth', 'binder', '$rootScope',
            function (requestStatus, $scope, $anchorScroll, $timeout,
                      $route, $translate, syncTranslate, $sce, auth, binder, $rootScope) {
                var self = this;
                this.reset = function () {
                    this.error = {};
                    this.loading = {};
                };
                this.convertMsg = function (msg) {
                    if (typeof msg == 'string') {
                        return {message: msg, params: {}};
                    } else if (msg instanceof Array) {
                        return {message: msg[0], params: msg[1]};
                    } else {
                        if (!msg.params) {
                            msg.params = {};
                        }
                        return msg;
                    }
                };
                this.scrollToError = function () {
                    $timeout(function () {
                        $anchorScroll('errorOutput');
                    }, 1);
                };
                this.strToErr = function (msg) {
                    return {message: msg, params: {}};
                };
                this.logout = function () {
                    auth.logout();
                };
                this.responseToErr = function (responseData, prefix) {
                    prefix = self.convertMsg(prefix);
                    if (typeof responseData == 'object') {
                        if (typeof responseData.message == 'string') {
                            var result = {};
                            result.message = responseData.message;
                            result.params = responseData.params || {};
                            result.causes = [];
                            if (responseData.field2Errors instanceof Object) {
                                for(var key in responseData.field2Errors) {
                                    var list = responseData.field2Errors[key];
                                    for (var k2 in list) {
                                        result.causes.push(self.convertMsg(list[k2]));
                                    }
                                }
                            }
                            return result;
                        } else {
                            return Object.assign(prefix, {verb: JSON.stringify(response.data)});
                        }
                    } else {
                        return Object.assign(prefix, {verb: $sce.trustAsHtml(responseData)});
                    }
                };
                self.reset();
                binder($scope, {
                    'event.request.started': (event, msg) => {
                        self.reset();
                        self.loading = self.convertMsg(msg ? msg : 'Loading');
                    },
                    'event.request.validation': (event, msg) => {
                        self.reset();
                        self.error = self.convertMsg(msg);
                        self.scrollToError();
                    },
                    'event.request.failed': (event, response) => {
                        self.reset();
                        if (response.status == 502 || response.status == -1) {
                            self.error = self.responseToErr(response.data, "Server is not available");
                        } else if (response.status == 401) {
                            self.error = self.responseToErr(response.data, 'authentication-error');
                        } else if (response.status == 403) {
                            self.error = self.responseToErr(response.data, 'authorization-error');
                        } else if (response.status == 404) {
                            self.error = self.responseToErr(response.data, 'entity-not-found');
                        } else if (response.status == 400) {
                            self.error = self.responseToErr(response.data, 'bad-request');
                        } else if (response.status == 500) {
                            self.error = self.responseToErr(response.data, 'application-error');
                        } else if (response.status < 299) {
                            self.error = self.responseToErr(response.data,
                                                            ['no-error-but-failed',
                                                             {status: response.status}]);
                        } else if (!response.status) {
                            console.log("no status message: " + response.message);
                            self.error = self.responseToErr(response.data, 'status-is-missing');
                        } else {
                            self.error = self.responseToErr(response.data,
                                                            ['unexpected-status', {status: response.status}]);
                        }
                        self.error.status = response.status;
                        self.scrollToError();
                    },
                    'event.request.complete': (event, response) => {
                        self.reset();
                        self.error = 0;
                    }
                });
                $rootScope.$broadcast('event.request.status.ready');
            }]});
