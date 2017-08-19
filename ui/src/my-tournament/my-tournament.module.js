import angular from 'angular';

angular.module('myTournament', [
    'ngRoute', 'core.tournament', 'mainMenu',
    'auth', 'core.requestStatus'
]);
