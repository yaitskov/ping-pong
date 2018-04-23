import angular from 'angular';
import template from './pick-group-rules-dialog.template.html';
import PickGroupRulesDialog from './PickGroupRulesDialog.js';

angular.
    module('tournament').
    component('pick-group-rules-dialog', {
        templateUrl: template,
        controller: PickGroupRulesDialog});
