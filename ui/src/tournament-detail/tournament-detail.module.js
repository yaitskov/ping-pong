import angular from 'angular';

angular.module('tournamentDetail', [
    'ngRoute', 'core.tournament', 'mainMenu', 'auth', 'core',
    'core.requestStatus', 'core.tournamentStatus'
]);
