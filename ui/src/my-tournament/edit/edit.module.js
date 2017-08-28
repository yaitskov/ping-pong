import angular from 'angular';

angular.module('tournamentEdit', [
    'ngRoute', 'core.tournament', 'mainMenu',
    'auth', 'core.requestStatus', 'core.validate',
    'core.util',
    'core', 'core.place', 'placePicker', 'core.tournament'
]);
