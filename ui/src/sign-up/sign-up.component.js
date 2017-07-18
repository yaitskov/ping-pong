'use strict';

angular.
    module('signUp').
    component('signUp', {
        templateUrl: 'sign-up/sign-up.template.html',
        controller: [
            'mainMenu', '$http', 'cutil', 'auth', 'requestStatus',
            function (mainMenu, $http, cutil, auth, requestStatus) {
                mainMenu.setTitle('Sign Up');
                this.account = {};
                this.errors = {};
                var self = this;
                requestStatus.setup('Account', 'Email is already used');
                requestStatus.complete();
                this.validateName = function () {
                    if (!self.account.firstName) {
                        self.errors.firstName = true;
                        requestStatus.validationFailed('First name is required');
                        return;
                    }
                    if (!self.account.lastName) {
                        self.errors.lastName = true;
                        requestStatus.validationFailed('Last name is required');
                        return;
                    }
                    if (self.account.lastName.length < 2) {
                        self.errors.lastName = true;
                        requestStatus.validationFailed('Last name is too short');
                        return;
                    }
                    if (self.account.firstName.length < 2) {
                        self.errors.firstName = true;
                        requestStatus.validationFailed('First name is too short');
                        return;
                    }
                    if (self.account.firstName.length + self.account.lastName.length > 39) {
                        self.errors.lastName = true;
                        self.errors.firstName = true;
                        requestStatus.validationFailed('Name is too long');
                        return;
                    }
                    return true;
                };
                this.validatePhone = function () {
                    if (self.account.phone) {
                        if (self.account.phone.length < 5) {
                            self.errors.phone = true;
                            requestStatus.validationFailed('Phone is too short');
                            return;
                        }
                        if (self.account.phone.length > 20) {
                            self.errors.phone = true;
                            requestStatus.validationFailed('Phone is too long');
                            return;
                        }
                        if (!self.account.phone.match('^[+]?[0-9 -]+$')) {
                            self.errors.phone = true;
                            requestStatus.validationFailed('Phone is in wrong format');
                            return;
                        }
                    }
                    return true;
                };
                this.validateEmail = function () {
                    if (self.account.email) {
                        if (self.account.email.length > 60) {
                            self.errors.email = true;
                            requestStatus.validationFailed('Email is too long');
                            return;
                        }
                        if (!self.account.email.match('^[a-zA-Z0-9._]+[@][a-zA-Z0-9._-]+[.][a-z]{2,3}$')) {
                            self.errors.email = true;
                            requestStatus.validationFailed('Email has wrong format');
                            return;
                        }
                    }
                    return true;
                };
                this.validate = function () {
                    self.errors = {};
                    return self.validateEmail() &&
                        self.validateName() &&
                        self.validatePhone();
                };
                this.signUp = function () {
                    requestStatus.startLoading('Registering account');
                    if (!self.validate()) {
                        return;
                    }
                    var userName = this.account.firstName + ' ' + this.account.lastName;
                    $http.post('/api/anonymous/user/register',
                               {name: userName,
                                email: self.account.email,
                                phone: self.account.phone,
                                sessionPart: cutil.genUserSessionPart()
                               },
                               {'Content-Type': 'application/json'}).
                        then(
                            function (okResp) {
                                auth.storeSession(okResp.data.session,
                                                  okResp.data.uid,
                                                  userName, self.account.email);
                            },
                            function (badResp) {
                                self.error = badResp.status;
                            });
                };
            }]});
