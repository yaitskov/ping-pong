import angular from 'angular';

angular.module('participant', [
    'ngRoute', 'core.tournament', 'mainMenu', 'auth', 'core',
    'core.requestStatus', 'core.participant'
]);
