'use strict';

angular.
    module('signUp').
    component('signUp', {
        templateUrl: 'sign-up/sign-up.template.html',
        controller: [
            'mainMenu', '$http', 'cutil', 'auth',
            function (mainMenu, $http, cutil, auth) {
                mainMenu.setTitle('Sign Up');
                var self = this;
                this.error = null;
                this.signUp = function () {
                    var userName = this.firstName + ' ' + this.lastName;
                    $http.post('/api/anonymous/user/register',
                               {name: userName,
                                email: this.email,
                                phone: this.phone,
                                sessionPart: cutil.genUserSessionPart()
                               },
                               {'Content-Type': 'application/json'}).
                        then(
                            function (okResp) {
                                auth.storeSession(okResp.data.session,
                                                  okResp.data.uid,
                                                  userName, self.email);
                            },
                            function (badResp) {
                                self.error = badResp.status;
                            });
                };
            }]});
