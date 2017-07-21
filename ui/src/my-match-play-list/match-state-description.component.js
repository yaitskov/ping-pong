'use strict';

angular.module('matchStateDescription').
    component('matchStateDescription', {
        templateUrl: 'my-match-play-list/match-state-description.template.html',
        controller: ['mainMenu',
                     function (mainMenu) {
                         mainMenu.setTitle("Meaning of states in a match");
                     }]
    });
