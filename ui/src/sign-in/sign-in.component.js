'use strict';

angular.
    module('signIn').
    component('signIn', {
        templateUrl: 'sign-in/sign-in.template.html',
        controller: [
            'mainMenu', '$http', 'auth',
            function (mainMenu, $http, auth) {
                mainMenu.setTitle('Sign In');
                var self = this;
                this.email = auth.myEmail()
                this.ok = null;
                this.error = null;
                this.signIn = function () {
                    this.ok = null;
                    this.error = null;
                    $http.post('/api/anonymous/auth/generate/sign-in-link',
                               self.email,
                               {'Content-Type': 'application/json'}).
                        then(
                            function (ok) {
                                self.ok = 1;
                            },
                            function (bad) {
                                self.error = "Failed " + bad.status;
                            });
                };
            }]});
