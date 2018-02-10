import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './seeding-tr-params.template.html';
import SeedingTournamentParamsCtrl from './SeedingTournamentParamsCtrl.js';

angular.
    module('tournament').
    component('seedingTrParams', {
        templateUrl: template,
        require: {
            parent: '^^tournamentParametersForm'
        },
        controller: SeedingTournamentParamsCtrl});
