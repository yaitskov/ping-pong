'use strict';

angular.module('emailsWithSignInTokenList').
    component('emailsWithSignInTokenList', {
        templateUrl: 'emails-with-sign-in-token-list/emails-with-sign-in-token-list.template.html',
        controller: ['$location', '$http',
                     function ($location, $http) {
                         this.error = null;
                         this.emails = null;
                         var self = this;
                         this.signIn = function (email, token, idx) {
                             $http.get('/api/anonymous/auth/by-one-time-token/' + token + '/' + email).then(
                                 function (resp) {
                                     localStorage.setItem('mySession', resp.data.session);
                                     localStorage.setItem('myUid', resp.data.session);
                                     localStorage.setItem('myEmail', email);
                                     self.emails.splice(idx, 1);
                                 },
                                 function (bad) {
                                     if (bad.status == 502) {
                                         self.error = "Server is not available";
                                     } else if (bad.status = 401) {
                                         self.error = "Link is not valid any more";
                                         self.emails.splice(idx, 1);
                                     } else {
                                         self.error = "Failed " + bad.status + " " + bad.data;
                                     }

                                 });
                         };
                         $http.get('/api/dev/emails-with-sign-in-token', {}).
                             then(
                                 function (ok) {
                                     self.emails = ok.data;
                                 },
                                 function (bad) {
                                     if (bad.status == 502) {
                                         self.error = "Server is not available";
                                     } else {
                                         self.error = "Failed with status " + bad.status + " " + bad.data;
                                     }
                                 })
                     }]});
