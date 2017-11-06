import angular from 'angular';
import template from './account-edit.template.html';

angular.
    module('user').
    component('accountEdit', {
        templateUrl: template,
        controller: [
            'mainMenu', 'auth', 'requestStatus', 'User', '$http',
            function (mainMenu, auth, requestStatus, User, $http) {
                mainMenu.setTitle('Account Modification');
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
                                requestStatus.failed);
                };
                requestStatus.startLoading();
                $http.get('/api/anonymous/user/info/by/session/' + auth.mySession()).
                    then(
                        function (ok) {
                            requestStatus.complete();
                            self.data.phone = ok.data.phone;
                        },
                        requestStatus.failed);
            }]});
