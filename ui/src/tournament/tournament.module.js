import angular from 'angular';

angular.module('tournament', ['ngRoute', 'participant', 'core.tournament', 'mainMenu',
                              'auth', 'scoreSet',
                              'core.requestStatus', 'core.match', 'core.requestStatus']);
