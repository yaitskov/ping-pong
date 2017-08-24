import angular from 'angular';
import template from './account.template.html';

angular.
    module('account').
    component('account', {
        templateUrl: template,
        controller: [
            'mainMenu', '$http', 'cutil', 'auth', 'requestStatus', 'pageCtx', 'syncTranslate',
            function (mainMenu, $http, cutil, auth, requestStatus, pageCtx, syncTranslate) {
                mainMenu.setTitle('Account Details');
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
                        auth.setMyLang(originLanguage);
                        self.language = language;
                    });
                };
                this.setLanguage(auth.myLang());
                this.logoutWithoutEmail = false;
                this.signInEmailSent = false;
                requestStatus.startLoading();
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
                            requestStatus.failed);
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
