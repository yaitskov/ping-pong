import angular from 'angular';
import template from './sign-in.template.html';

angular.
    module('signIn').
    component('signIn', {
        templateUrl: template,
        controller: [
            'mainMenu', '$http', 'auth', 'requestStatus',
            function (mainMenu, $http, auth, requestStatus) {
                mainMenu.setTitle('Sign In');
                var self = this;
                this.email = auth.myEmail()
                this.form = null;
                this.ok = null;
                this.signIn = function () {
                    if (!self.form.$valid) {
                        return;
                    }
                    requestStatus.startLoading("Sending email");
                    this.ok = null;
                    $http.post('/api/anonymous/auth/generate/sign-in-link',
                               self.email,
                               {'Content-Type': 'application/json'}).
                        then(
                            function (ok) {
                                self.ok = 1;
                                requestStatus.complete(ok);
                            },
                            requestStatus.failed);
                };
            }]});
