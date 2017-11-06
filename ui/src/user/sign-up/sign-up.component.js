import angular from 'angular';
import template from './sign-up.template.html';

angular.
    module('user').
    component('signUp', {
        templateUrl: template,
        controller: [
            'mainMenu', '$http', 'cutil', 'auth', 'requestStatus', 'binder', '$scope',
            function (mainMenu, $http, cutil, auth, requestStatus, binder, $scope) {
                binder($scope, {
                    'event.main.menu.ready': (e) => mainMenu.setTitle('Sign Up btn')});
                this.form = {};
                var self = this;
                this.signUp = function (form) {
                    self.form.$setSubmitted();
                    if (!self.form.$valid) {
                        return;
                    }
                    requestStatus.startLoading('Registering account');
                    var userName = self.firstName + ' ' + self.lastName;
                    $http.post('/api/anonymous/user/register',
                               {name: userName,
                                email: self.email,
                                phone: self.phone,
                                sessionPart: cutil.genUserSessionPart()
                               },
                               {'Content-Type': 'application/json'}).
                        then(
                            function (okResp) {
                                requestStatus.complete(okResp);
                                auth.storeSession(okResp.data.session,
                                                  okResp.data.uid,
                                                  userName, self.email,
                                                  okResp.data.type);
                            },
                            requestStatus.failed);
                };
            }]});
