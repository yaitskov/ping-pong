import angular from 'angular';
import template from './do-sign-in.template.html';

angular.
    module('user').
    component('doSignIn', {
        templateUrl: template,
        controller: [
            'mainMenu', '$routeParams', '$location', '$http', 'auth', 'requestStatus', 'binder', '$scope',
            function (mainMenu, $routeParams, $location, $http, auth, requestStatus, binder, $scope) {
                var self = this;
                binder($scope, {
                    'event.main.menu.ready': (e) => mainMenu.setTitle('Authentication'),
                    'event.request.status.ready': (event) => {
                        requestStatus.startLoading('Authentication');
                        $http.get('/api/anonymous/auth/by-one-time-token/'
                                  + $routeParams.oneTimeSignInToken
                                  + '/'
                                  + $routeParams.email).
                            then(
                                function (resp) {
                                    requestStatus.complete();
                                    auth.storeSession(resp.data.session,
                                                      resp.data.uid,
                                                      resp.data.name,
                                                      $routeParams.email,
                                                      resp.data.type);
                                    $location.path('/');
                                },
                                requestStatus.failed);
                    }
                });
            }]});
