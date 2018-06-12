import angular from 'angular';
import AppLang from 'ui/lang.js';
import template from './account.template.html';

angular.
    module('user').
    component('account', {
        templateUrl: template,
        controller: [
            'mainMenu', '$http', 'cutil', 'auth', 'requestStatus', 'pageCtx', 'syncTranslate', 'binder', '$scope',
            function (mainMenu, $http, cutil, auth, requestStatus, pageCtx, syncTranslate, binder, $scope) {
                var stranslate = syncTranslate.create();
                var self = this;
                this.uid = auth.myUid();
                this.session = auth.mySession();
                this.name = auth.myName();
                this.email = auth.myEmail();
                this.isAdmin = auth.isAdmin();
                this.adminAccessRequested = pageCtx.get('amdin-access-requested');
                this.userData = null;
                this.language = null;
                this.setLanguage = function (originLanguage) {
                    stranslate.trans(originLanguage, function (language) {
                        AppLang.setLanguage(originLanguage);
                        self.language = language;
                    });
                };
                this.setLanguage(AppLang.getLanguage());
                this.logoutWithoutEmail = false;
                this.signInEmailSent = false;
                this.requestAdminPermissions = function () {
                    requestStatus.startLoading("Requesting admin access");
                    $http.post('/api/user/request-admin-access',
                               {},
                               {
                                   headers: {session: auth.mySession()}
                               })
                        .then(
                            function (ok) {
                                pageCtx.put('amdin-access-requested', 1);
                                self.adminAccessRequested = 1;
                                requestStatus.complete();
                            },
                          (...a) => requestStatus.failed(...a));
                };
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
                          (...a) => requestStatus.failed(...a));
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
                binder($scope, {
                    'event.main.menu.ready': (e) => mainMenu.setTitle('Account Details'),
                    'event.request.status.ready': (event) => {
                        requestStatus.startLoading();
                        $http.get('/api/anonymous/user/info/by/session/' + auth.mySession()).
                            then(
                                function (ok) {
                                    requestStatus.complete();
                                    self.userData = ok.data;
                                },
                                (...a) => requestStatus.failed(...a));
                    }
                });
            }]});
