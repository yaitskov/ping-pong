import angular from 'angular';
import template from './group-rule-parameters-dialog.template.html';
import GroupRuleParametersDialog from './GroupRuleParametersDialog.js';

angular.
    module('tournament').
    component('groupRuleParametersDialog', {
        templateUrl: template,
        controller: GroupRuleParametersDialog});
