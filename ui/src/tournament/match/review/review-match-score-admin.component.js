import angular from 'angular';
import template from './review-match-score-admin.template.html';

angular.
    module('tournament').
    component('reviewMatchScoreForAdmin', {
        templateUrl: template,
        controller: ['mainMenu', '$routeParams',
                     function (mainMenu, $routeParams) {
                         mainMenu.setTitle('Match Review');
                         this.tournamentId = $routeParams.tournamentId;
                     }]});
