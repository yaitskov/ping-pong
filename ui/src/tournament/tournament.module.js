import angular from 'angular';

angular.module('tournament', ['ngRoute', 'participant', 'core.tournament', 'mainMenu',
                              'auth',
                              'core.requestStatus', 'core.match', 'core.requestStatus']);
