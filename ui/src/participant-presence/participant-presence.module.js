import angular from 'angular';

angular.module('participantPresence',
               ['ngRoute', 'core.tournament',
                'mainMenu', 'auth', 'core.requestStatus',
                'core.participant']);
