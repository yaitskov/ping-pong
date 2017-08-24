import angular from 'angular';
import template from './sign-up.template.html';

angular.
    module('signUp').
    component('signUp', {
        templateUrl: template,
        controller: [
            'mainMenu', '$http', 'cutil', 'auth', 'requestStatus',
            function (mainMenu, $http, cutil, auth, requestStatus) {
                mainMenu.setTitle('Sign Up');
                this.form = {};
                var self = this;
                this.signUp = function (form) {
                    self.form.$setSubmitted();
                    if (!self.form.$valid) {
                        return;
                    }
                    requestStatus.startLoading('Registering account');
                    var userName = self.firstName + ' ' + self.lastName;
                    $http.post('/api/anonymous/user/register',
                               {name: userName,
                                email: self.email,
                                phone: self.phone,
                                sessionPart: cutil.genUserSessionPart()
                               },
                               {'Content-Type': 'application/json'}).
                        then(
                            function (okResp) {
                                requestStatus.complete(okResp);
                                auth.storeSession(okResp.data.session,
                                                  okResp.data.uid,
                                                  userName, self.email, 'User');
                            },
                            requestStatus.failed);
                };
            }]});
