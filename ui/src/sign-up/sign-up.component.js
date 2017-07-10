'use strict';

angular.
    module('signUp').
    component('signUp', {
        templateUrl: 'sign-up/sign-up.template.html',
        controller: [
            'mainMenu', '$http', 'cutil', 'auth',
            function (mainMenu, $http, cutil, auth) {
                mainMenu.setTitle('Sing Up');
                var self = this;
                this.error = null;
                this.signUp = function () {
                    console.log("Sign up");
                    $http.post('/api/anonymous/user/register',
                               {name: this.firstName + ' ' + this.lastName,
                                email: this.email,
                                phone: this.phone,
                                sessionPart: cutil.genUserSessionPart()
                               },
                               {'Content-Type': 'application/json'}).
                        then(
                            function (okResp) {
                                auth.storeSessionAndUid(okResp.data.session, okResp.data.uid);
                            },
                            function (badResp) {
                                self.error = badResp.status;
                            });
                };
            }]});
