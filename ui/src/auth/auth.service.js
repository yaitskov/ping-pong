import angular from 'angular';

angular.
    module('auth').
    factory('auth', ['LocalStorage', '$location',
                     function (LocalStorage, $location) {
                         function auth() {
                             this.returnOnAuth = null;
                             this.isAuthenticated = function () {
                                 return !!LocalStorage.get('mySession');
                             };
                             this.isAdmin = function () {
                                 return !!LocalStorage.get('admin') ||
                                     !!LocalStorage.get('adminOfTournaments');
                             };
                             this.userType = function () {
                                 return LocalStorage.get('myType') || 'User';
                             };
                             this.logout = function () {
                                 LocalStorage.clearAll();
                                 $location.path('/');
                             };
                             this.myEmail = function () {
                                 return LocalStorage.get('myEmail');
                             };
                             this.mySession = function() {
                                 var value = LocalStorage.get('mySession');
                                 console.log("session is " + value);
                                 return value;
                             };
                             this.myName = function () {
                                 return LocalStorage.get('myName');
                             };
                             this.myUid = function() {
                                 return LocalStorage.get('myUid');
                             };
                             this.requireLogin = function () {
                                 console.log("auth is required");
                                 this.returnOnAuth = $location.path();
                                 $location.path('/sign-up');
                             };
                             this.updateAccount = function (accountInfo) {
                                 if (accountInfo.email) {
                                     LocalStorage.store('myEmail', accountInfo.email);
                                 } else {
                                     LocalStorage.drop('myEmail');
                                 }
                                 LocalStorage.store('myName', accountInfo.name);
                             }
                             this.storeSession = function (fullSession, uid, name, email, type) {
                                 console.log("Authenticated as " + fullSession);
                                 LocalStorage.store('mySession', fullSession);
                                 LocalStorage.store('myUid', uid);
                                 LocalStorage.store('myName', name);
                                 if (email) {
                                     LocalStorage.store('myEmail', email);
                                 }
                                 LocalStorage.store('myType', type);
                                 if (this.returnOnAuth) {
                                     $location.path(this.returnOnAuth);
                                     this.returnOnAuth = null
                                 } else {
                                     $location.path('/');
                                 }
                             };
                         }
                         return new auth();
                     }
                    ]);
