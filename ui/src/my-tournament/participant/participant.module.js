import angular from 'angular';

angular.module('participant',
               ['ngRoute', 'mainMenu', 'auth', 'core.requestStatus',
               'core.participant', 'core']);
