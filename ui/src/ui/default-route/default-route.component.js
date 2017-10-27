import angular from 'angular';

angular.
    module('ui').
    component('defaultRoute', {
        controller: ['auth', '$location', function (auth, $location) {
            if (auth.userType() == 'Admin') {
                $location.path('/my/tournaments');
            } else if (auth.isAuthenticated()) {
                $location.path('/play/in/tournaments');
            } else {
                $location.path('/tournament/draft/list');
            }
        }]});
