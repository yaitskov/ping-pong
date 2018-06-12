import angular from 'angular';
import template from './sign-in.template.html';

angular.
    module('user').
    component('signIn', {
        templateUrl: template,
        controller: [
            'mainMenu', '$http', 'auth', 'requestStatus', 'binder', '$scope',
            function (mainMenu, $http, auth, requestStatus, binder, $scope) {
                binder($scope, {
                    'event.main.menu.ready': (e) => mainMenu.setTitle('Sign In')});
                var self = this;
                this.email = auth.myEmail()
                this.form = null;
                this.ok = null;
                this.signIn = function () {
                    if (!self.form.$valid) {
                        return;
                    }
                    requestStatus.startLoading("Sending email");
                    self.ok = null;
                    $http.post('/api/anonymous/auth/generate/sign-in-link',
                               self.email,
                               {'Content-Type': 'application/json'}).
                        then(
                            function (ok) {
                                self.ok = 1;
                                requestStatus.complete(ok);
                            },
                            (...a) => requestStatus.failed(...a));
                };
            }]});
