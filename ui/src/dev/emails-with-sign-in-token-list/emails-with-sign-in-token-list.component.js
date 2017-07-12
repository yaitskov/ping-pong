'use strict';

angular.module('emailsWithSignInTokenList').
    component('emailsWithSignInTokenList', {
        templateUrl: 'emails-with-sign-in-token-list/emails-with-sign-in-token-list.template.html',
        controller: ['$location', '$http',
                     function ($location, $http) {
                         this.error = null;
                         this.emails = null;
                         var self = this;
                         this.signIn = function (email, token) {
                             $http.get('/api/anonymous/auth/by-one-time-token/' + token + '/' + email).then(
                                 function (resp) {
                                     localStorage.putItem('mySession', resp.data.session);
                                     localStorage.putItem('myUid', resp.data.session);
                                     localStorage.putItem('myEmail', email);
                                 },
                                 function (bad) {
                                     self.error = "Failed " + bad.status + " " + bad.data;
                                 });
                         };
                         $http.get('/api/dev/emails-with-sign-in-token', {}).
                             then(
                                 function (ok) {
                                     self.emails = ok.data;
                                 },
                                 function (bad) {
                                     self.error = "Failed with status " + bad.status + " " + bad.data;
                                 })
                     }]});
