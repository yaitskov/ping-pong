'use strict';

angular.module('emailsWithSignInTokenList').
    component('emailsWithSignInTokenList', {
        templateUrl: 'emails-with-sign-in-token-list/emails-with-sign-in-token-list.template.html',
        cache: false,
        controller: ['$location', '$http',
                     function ($location, $http) {
                         this.error = null;
                         this.emails = null;
                         var self = this;
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
