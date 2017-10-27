import angular from 'angular';
import template from './review-match-score-user.template.html';

angular.
    module('tournament').
    component('reviewMatchScoreForUser', {
        templateUrl: template,
        controller: ['mainMenu', '$routeParams',,
                     function (mainMenu, $routeParams) {
                         mainMenu.setTitle('Match Review');
                         this.tournamentId = $routeParams.tournamentId;
                     }]});
