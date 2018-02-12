import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './console-tr-params.template.html';
import ConsoleParamsCtrl from './ConsoleParamsCtrl.js';

angular.
    module('tournament').
    component('consoleTrParams', {
        require: {
            parent: '^^tournamentParametersForm'
        },
        templateUrl: template,
        controller: ConsoleParamsCtrl});
