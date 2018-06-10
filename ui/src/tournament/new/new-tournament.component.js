import angular from 'angular';
import '../tournament.scss';
import template from './new-tournament.template.html';
import NewTournamentCtrl from './NewTournamentCtrl.js';

angular.
    module('tournament').
    component('newTournament', {
        templateUrl: template,
        controller: NewTournamentCtrl});
