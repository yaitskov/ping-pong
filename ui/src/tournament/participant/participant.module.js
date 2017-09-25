import angular from 'angular';

angular.module('participant', [
    'ngRoute', 'ui.sortable',
    'core.tournament', 'mainMenu', 'auth', 'core',
    'core.requestStatus', 'core.participant',
    'core.tournamentStatus'
]);
