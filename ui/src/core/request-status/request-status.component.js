import angular from 'angular';
import template from './request-status.template.html';

angular.
    module('core.requestStatus').
    component('requestStatus', {
        templateUrl: template,
        controller: [
            'requestStatus', '$rootScope', '$anchorScroll', '$timeout', '$route', '$translate',
            function (requestStatus, $scope, $anchorScroll, $timeout, $route, $translate) {
                var self = this;
                this.reset = function () {
                    this.error = null;
                    this.loading = null;
                };
                this.refresh = function () {
                    $route.reload();
                }
                this.scrollToError = function () {
                    $timeout(function () {
                        $anchorScroll('errorOutput');
                    }, 1);
                };
                self.reset();
                $scope.$on('event.request.started', function (event, msg) {
                    self.reset();
                    if (msg) {
                        self.loading = msg;
                    } else {
                        $translate('Loading').then(function (loading) { self.loading = loading; });
                    }
                });
                $scope.$on('event.request.validation', function (event, msg) {
                    self.reset();
                    self.error = msg;
                    self.scrollToError();
                });
                $scope.$on('event.request.failed', function (event, response) {
                    self.reset();
                    if (response.status == 502 || response.status == -1) {
                        $translate("Server is not available").then(function (err) {
                            self.error = err;
                        });
                    } else if (response.status == 401) {
                        $translate('session-expired').then(function (err) {
                            self.error = err;
                        });
                    } else if (response.status == 403) {
                        if (typeof response.data == 'object') {
                            if (response.data.message) {
                                $translate('no-permissions').then(function (err) {
                                    self.error = err + ": " + response.data.message;
                                });
                            } else {
                                $translate('application-error').then(function (err) {
                                    self.error = err + ": " + JSON.stringify(response.data);
                                });
                            }
                        } else {
                            self.error = "Bad request: " + response.data;
                        }
                    } else if (response.status == 404) {
                        if (typeof response.data == 'object') {
                            if (response.data.message) {
                                self.error = response.data.message;
                            } else {
                                self.error = "Entity not found: " + JSON.stringify(response.data);
                            }
                        } else {
                            self.error = "API method doesn't exist: " + response.data;
                        }
                    } else if (response.status == 400) {
                        if (typeof response.data == 'object') {
                            if (response.data.message) {
                                self.error = response.data.message;
                            } else {
                                self.error = "An application error happened: " + JSON.stringify(response.data);
                            }
                        } else {
                            self.error = "Bad request: " + response.data;
                        }
                    } else if (response.status == 500) {
                        if (typeof response.data == 'string') {
                            self.error = "An application error happened:\n" + response.data;
                        } else if (typeof response.data == 'object') {
                            if (response.data.message) {
                                self.error = response.data.message;
                            } else {
                                self.error = "An application error happened: " + JSON.stringify(response.data);
                            }
                        } else {
                            self.error = "An application error happened.";
                        }
                    } else if (response.status < 299) {
                        self.error = "Status " + response.status +
                            " looks good, but request failed.";
                    } else if (!response.status) {
                        self.error = "Status is missing";
                    } else {
                        self.error = "Failed with unexpected status " + response.status;
                        if (typeof response.data == 'string') {
                            self.error = self.error + ": " + response.data;
                            console.log("Failed with " + response.status + ": " + response.data);
                        } else if (typeof response.data == 'object') {
                            if (response.data.message) {
                                self.error = self.error + ": " + response.data.message;
                            } else {
                                self.error = self.error + ":\n " + JSON.stringify(response.data);
                            }
                            console.log("Failed with " + response.status + ": "
                                        + JSON.stringify(response.data));
                        }
                    }
                    self.scrollToError();
                });
                $scope.$on('event.request.complete', function (event, response) {
                    self.reset();
                    self.error = 0;
                });
            }]});
