'use strict';

angular.
    module('core.requestStatus').
    component('requestStatus', {
        templateUrl: 'core/request-status/request-status.template.html',
        controller: [
            'requestStatus', '$rootScope', '$anchorScroll', '$timeout',
            function (requestStatus, $scope, $anchorScroll, $timeout) {
                this.reset = function () {
                    this.error = null;
                    this.loading = null;
                };
                this.scrollToError = function () {
                    $timeout(function () {
                        $anchorScroll('errorOutput');
                    }, 1);
                };
                var self = this;
                self.reset();
                $scope.$on('event.request.started', function (event, msg) {
                    self.reset();
                    self.loading = msg || 'Loading';
                });
                $scope.$on('event.request.validation', function (event, msg) {
                    self.reset();
                    self.error = msg;
                    self.scrollToError();
                });
                $scope.$on('event.request.failed', function (event, response) {
                    self.reset();
                    if (response.status == 502) {
                        self.error = "Server is not available";
                    } else if (response.stutus == 401) {
                        self.error = "Session is not valid. Try to logout and login again if your account is bound to an email.";
                    } else if (response.status == 404) {
                        self.error = requestStatus.entity() + " does not exist";
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
                        self.error = "Status looks good";
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
