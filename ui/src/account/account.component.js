'use strict';

angular.
    module('account').
    component('account', {
        templateUrl: 'account/account.template.html',
        controller: [
            'mainMenu', '$http', 'cutil', 'auth', 'requestStatus',
            function (mainMenu, $http, cutil, auth, requestStatus) {
                mainMenu.setTitle('Account Details');
                var self = this;
                this.uid = auth.myUid();
                this.session = auth.mySession();
                this.name = auth.myName();
                this.email = auth.myEmail();
                this.userData = null;
                this.logoutWithoutEmail = false;
                this.signInEmailSent = false;
                requestStatus.startLoading();
                this.generateSignInLink = function () {
                    requestStatus.startLoading("Sending email");
                    self.signInEmailSent = false;
                    $http.post('/api/anonymous/auth/generate/sign-in-link',
                               self.email,
                               {'Content-Type': 'application/json'}).
                        then(
                            function (ok) {
                                self.signInEmailSent = true;
                                requestStatus.complete(ok);
                            },
                            requestStatus.failed);
                };
                this.logout = function () {
                    if (self.email) {
                        self.lostAccessToAccount();
                    } else {
                        self.logoutWithoutEmail = true;
                    }
                };
                this.lostAccessToAccount = function () {
                    auth.logout();
                };
                $http.get('/api/anonymous/user/info/by/session/' + auth.mySession()).
                    then(
                        function (ok) {
                            requestStatus.complete();
                            self.userData = ok.data;
                        },
                        requestStatus.failed);
            }]});
