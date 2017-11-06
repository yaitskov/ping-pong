import angular from 'angular';
import template from './match-state-description.template.html';

angular.module('tournament').
    component('matchStateDescription', {
        templateUrl: template,
        controller: ['mainMenu', 'binder', '$scope', '$rootScope',
                     function (mainMenu, binder, $scope, $rootScope) {
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle("Meaning of states in a match")});
                     }]
    });
