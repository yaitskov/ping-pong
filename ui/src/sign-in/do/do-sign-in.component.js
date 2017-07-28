'use strict';

angular.
    module('doSignIn').
    component('doSignIn', {
        templateUrl: 'sign-in/do/do-sign-in.template.html',
        controller: [
            'mainMenu', '$routeParams', '$location', '$http', 'auth',
            function (mainMenu, $routeParams, $location, $http, auth) {
                mainMenu.setTitle('Authentication...');
                var self = this;
                this.error = null;
                $http.get('/api/anonymous/auth/by-one-time-token/'
                          + $routeParams.oneTimeSignInToken
                          + '/'
                          + $routeParams.email).
                    then(
                        function (resp) {
                            auth.storeSession(resp.data.session,
                                              resp.data.uid,
                                              resp.data.name,
                                              $routeParams.email,
                                              resp.data.type);
                            $location.path('/');
                        },
                        function (bad) {
                            if (bad.status == 502) {
                                self.error = "Server is not available";
                            } else if (bad.status = 401) {
                                self.error = "Authentication failed due token is not valid";
                                self.emails.splice(idx, 1);
                            } else {
                                self.error = "Failed " + bad.status + " " + bad.data;
                            }
                        });
            }]});
