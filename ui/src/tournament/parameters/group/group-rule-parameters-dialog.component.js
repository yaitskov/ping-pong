import angular from 'angular';
import template from './group-rule-parameters-dialog.template.html';
import GroupRuleParametersDialog from './GroupRuleParametersDialog.js';

angular.
    module('tournament').
    component('group-rule-parameters-dialog', {
        templateUrl: template,
        controller: GroupRuleParametersDialog});
