import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './enlist-params.template.html';
import EnlistParamsCtrl from './EnlistParamsCtrl.js';

angular.
    module('tournament').
    component('enlistParams', {
        templateUrl: template,
        require: {
            parent: '^^tournamentParametersForm'
        },
        controller: EnlistParamsCtrl});
