import angular from 'angular';

angular.module('openTournament', [
    'ngRoute', 'core.match', 'core.tournament',
    'mainMenu', 'auth', 'core.requestStatus'
]);
