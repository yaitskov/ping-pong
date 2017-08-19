import angular from 'angular';

angular.module('tournamentParameters', [
    'ngRoute', 'core.tournament', 'mainMenu',
    'auth', 'core.requestStatus', 'core.validate',
    'core', 'core.tournament'
]);
