'use strict';

angular.
    module('account').
    component('account', {
        templateUrl: 'account/account.template.html',
        controller: [
            'mainMenu', '$http', 'cutil', 'auth',
            function (mainMenu, $http, cutil, auth) {
                mainMenu.setTitle('Account Details');
                var self = this;
                this.uid = auth.myUid();
                this.session = auth.mySession();
                this.name = auth.myName();
                this.email = auth.myEmail();
                this.userData = null;
                this.error = null;
                $http.get('/api/anonymous/user/info/by/session/' + auth.mySession()).
                    then(
                        function (ok) {
                            self.error = 0;
                            self.userData = ok.data;
                        },
                        function (bad) {
                            self.error = "Failed with status " + bad.status;
                        });

            }]});
