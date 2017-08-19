import angular from 'angular';
import template from './match-state-description.template.html';

angular.module('matchStateDescription').
    component('matchStateDescription', {
        templateUrl: template,
        controller: ['mainMenu',
                     function (mainMenu) {
                         mainMenu.setTitle("Meaning of states in a match");
                     }]
    });
