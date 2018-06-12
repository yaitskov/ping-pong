import angular from 'angular';
import template from './account-edit.template.html';

angular.
    module('user').
    component('accountEdit', {
        templateUrl: template,
        controller: [
            'mainMenu', 'auth', 'requestStatus', 'User', '$http', 'binder', '$scope',
            function (mainMenu, auth, requestStatus, User, $http, binder, $scope) {
                var self = this;
                this.data = {}
                this.data.name = auth.myName();
                this.data.phone = null;
                this.data.email = auth.myEmail();
                this.change = function () {
                    requestStatus.startLoading("Saving");
                    User.change(this.data,
                                function (ok) {
                                    requestStatus.complete();
                                    auth.updateAccount(self.data);
                                    history.back();
                                },
                              (...a) => requestStatus.failed(...a));
                };
                binder($scope, {
                    'event.main.menu.ready': (e) => mainMenu.setTitle('Account Modification'),
                    'event.request.status.ready': (event) => {
                        requestStatus.startLoading();
                        $http.get('/api/anonymous/user/info/by/session/' + auth.mySession()).
                            then(
                                function (ok) {
                                    requestStatus.complete();
                                    self.data.phone = ok.data.phone;
                                },
                                (...a) => requestStatus.failed(...a));
                    }
                });
            }]});
