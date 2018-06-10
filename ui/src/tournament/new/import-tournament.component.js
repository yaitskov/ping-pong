import angular from 'angular';
import template from './import-tournament.template.html';
import ImportTournamentCtrl from './ImportTournamentCtrl.js';

angular.
    module('tournament').
    component('importTournament', {
        templateUrl: template,
        controller: ImportTournamentCtrl});
