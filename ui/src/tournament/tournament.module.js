import angular from 'angular';

angular.module('tournament', ['ngRoute', 'participant', 'core.tournament', 'mainMenu',
                              'auth', 'scoreSet', 'core', 'placePicker', 'core.validate', 'core.place',
                              'core.requestStatus', 'core.match', 'core.requestStatus',
                              'tournamentCategory']);
