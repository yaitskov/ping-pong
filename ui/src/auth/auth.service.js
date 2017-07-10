'use strict';

angular.
    module('auth').
    factory('auth', ['LocalStorage', '$location', '$http',
                     function (LocalStorage, $location, $http) {
                         function auth() {
                             this.returnOnAuth = null;
                             this.isAuthenticated = function () {
                                 return !!LocalStorage.get('mySession');
                             };
                             this.isAdmin = function () {
                                 return !!LocalStorage.get('admin') ||
                                     !!LocalStorage.get('adminOfTournaments');
                             };
                             this.logout = function () {
                                 LocalStorage.clearAll();
                                 $location.path('/');
                             };
                             this.mySession = function() {
                                 var value = LocalStorage.get('mySession');
                                 console.log("session is " + value);
                                 return value;
                             };
                             this.myUid = function() {
                                 return LocalStorage.get('myUid');
                             };
                             this.requireLogin = function () {
                                 console.log("auth is required");
                                 this.returnOnAuth = $location.path();
                                 $location.path('/sign-up');
                             };
                             this.storeSessionAndUid = function (fullSession, uid) {
                                 console.log("Authenticated as " + fullSession);
                                 LocalStorage.store('mySession', fullSession);
                                 LocalStorage.store('myUid', uid);
                                 if (this.returnOnAuth) {
                                     $location.path(this.returnOnAuth);
                                     this.returnOnAuth = null
                                 } else {
                                     $location.path('/');
                                 }
                             };
                             this.sendLoginLink = function (email, ack, nack) {
                                 console.log("Request auth link for email " + email);
                                 $http.post('/api/anonymous/auth/send-auth-link',
                                            {'email': email},
                                            {'Content-Type': 'application/json'}).
                                     then(
                                         function (response) {
                                             ack(response);
                                         },
                                         function (response) {
                                             console.log("Failed to send link for " + email);
                                             nack(response);
                                         });
                             };
                         }
                         return new auth();
                     }
                    ]);
