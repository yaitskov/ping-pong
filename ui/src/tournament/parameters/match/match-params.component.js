import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './match-params.template.html';
import MatchParamsCtrl from './MatchParamsCtrl.js';

angular.
    module('tournament').
    component('matchParams', {
        templateUrl: template,
        require: {
            parent: '^^tournamentParametersForm'
        },
        controller: MatchParamsCtrl});
