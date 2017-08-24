import angular from 'angular';
import template from './do-sign-in.template.html';

angular.
    module('doSignIn').
    component('doSignIn', {
        templateUrl: template,
        controller: [
            'mainMenu', '$routeParams', '$location', '$http', 'auth', 'requestStatus',
            function (mainMenu, $routeParams, $location, $http, auth, requestStatus) {
                mainMenu.setTitle('Authentication');
                var self = this;
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
            }]});
